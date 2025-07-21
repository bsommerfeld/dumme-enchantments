/*package gg.norisk.satisfying

import com.llamalad7.mixinextras.injector.wrapoperation.Operation
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import gg.norisk.enchantments.EnchantmentRegistry.animation
import gg.norisk.enchantments.EnchantmentUtils.getLevel
import gg.norisk.enchantments.RenderUtils
import gg.norisk.enchantments.StupidEnchantments.toId
import gg.norisk.enchantments.utils.Animation
import gg.norisk.enchantments.utils.BlockPosSerializer
import gg.norisk.enchantments.utils.BlockStateSerializer
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.fabricmc.fabric.api.event.client.player.ClientPlayerBlockBreakEvents
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.entity.StructureBlockBlockEntity
import net.minecraft.client.MinecraftClient
import net.minecraft.command.argument.BlockPosArgumentType
import net.minecraft.command.argument.IdentifierArgumentType
import net.minecraft.entity.Entity
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.structure.StructurePlacementData
import net.minecraft.structure.StructureTemplate
import net.minecraft.structure.processor.BlockRotStructureProcessor
import net.minecraft.text.Text
import net.minecraft.util.*
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3i
import net.minecraft.world.ServerWorldAccess
import net.silkmc.silk.commands.PermissionLevel
import net.silkmc.silk.commands.command
import net.silkmc.silk.core.kotlin.ticks
import net.silkmc.silk.core.task.infiniteMcCoroutineTask
import net.silkmc.silk.core.task.mcCoroutineTask
import net.silkmc.silk.network.packet.s2cPacket
import java.time.Duration
import java.util.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration
import kotlin.time.toKotlinDuration


object SatisfyingBlockAnimation {
    @Serializable
    data class BlockAnimation(
        @Serializable(with = BlockPosSerializer::class) val pos: BlockPos,
        @Serializable(with = BlockStateSerializer::class) val state: BlockState,
        val startAnimation: Animation,
        val startIdleAnimation: Animation? = null,
        val stopAnimation: Animation? = null,
        val stopIdleAnimation: Animation? = null
    ) {
        val hasOtherAnimations get() = startIdleAnimation != null && stopAnimation != null && stopIdleAnimation != null

        @Transient
        var currentAnimation = startAnimation
        fun getAnimation(): Animation {
            val startIdle = startIdleAnimation ?: return startAnimation
            val stopAnimation = stopAnimation ?: return startAnimation
            val stopIdle = stopIdleAnimation ?: return startAnimation
            if (currentAnimation.isDone) {
                currentAnimation = when (currentAnimation) {
                    startAnimation -> startIdleAnimation
                    startIdleAnimation -> stopAnimation
                    stopAnimation -> stopIdleAnimation
                    stopIdleAnimation -> startAnimation
                    else -> startAnimation
                }
                currentAnimation.reset()
            }
            return currentAnimation
        }
    }

    val animations = mutableSetOf<BlockAnimation>()
    val toClean = mutableListOf<Job>()
    val blockAnimationS2C = s2cPacket<BlockAnimation>("block-animation".toId())

    fun initClient() {
        ClientPlayConnectionEvents.DISCONNECT.register(ClientPlayConnectionEvents.Disconnect { handler, client ->
            animations.clear()
            toClean.forEach(Job::cancel)
            toClean.clear()
        })

        blockAnimationS2C.receiveOnClient { packet, context ->
            mcCoroutineTask(sync = true, client = true) {
                animations.add(packet)
            }
        }

        ClientPlayerBlockBreakEvents.AFTER.register(ClientPlayerBlockBreakEvents.After { world, player, pos, state ->
            if (animation.getLevel(player.getStackInHand(Hand.MAIN_HAND)) != null) {
                animations.add(
                    BlockAnimation(
                        pos, state, Animation(1f, 0f, 0.5.seconds, Animation.Easing.CUBIC_OUT),
                    )
                )
            }
        })

        WorldRenderEvents.LAST.register {
            val player = MinecraftClient.getInstance().player ?: return@register
            val matrixStack = it.matrixStack() ?: return@register
            animations.forEach { animation ->
                RenderUtils.renderBlock(
                    matrixStack,
                    Vec3d(animation.pos.x.toDouble(), animation.pos.y.toDouble(), animation.pos.z.toDouble()),
                    animation.state,
                    animation.pos
                ) { stack ->
                    val fade = animation.getAnimation().get()
                    stack.scale(fade, fade, fade)
                }
            }
            animations.removeIf { animation -> animation.startAnimation.isDone && !animation.hasOtherAnimations }
        }
    }

    fun initServer() {
        command("satisfying") {
            requiresPermissionLevel(PermissionLevel.OWNER)
            literal("place") {
                argument<Identifier>("template", IdentifierArgumentType.identifier()) {
                    suggestList {
                        it.source.world.structureTemplateManager.streamTemplates().toList()
                    }
                    argument<String>("easing") { easingString ->
                        suggestList { Animation.Easing.values().map { it.name } }
                        argument<Double>("duration") { duration ->
                            runs {
                                executePlaceTemplate(
                                    this.source,
                                    IdentifierArgumentType.getIdentifier(this, "template"),
                                    Animation.Easing.valueOf(easingString().uppercase()),
                                    duration().seconds.toJavaDuration()
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private val TEMPLATE_INVALID_EXCEPTION: DynamicCommandExceptionType =
        DynamicCommandExceptionType { `object`: Any? ->
            Text.stringifiedTranslatable(
                "commands.place.template.invalid",
                `object`
            )
        }
    private val TEMPLATE_FAILED_EXCEPTION: SimpleCommandExceptionType =
        SimpleCommandExceptionType(Text.translatable("commands.place.template.failed"))

    private fun throwOnUnloadedPos(serverWorld: ServerWorld, chunkPos: ChunkPos, chunkPos2: ChunkPos) {
        if (ChunkPos.stream(chunkPos, chunkPos2).filter { chunkPosx: ChunkPos ->
                !serverWorld.canSetBlock(
                    chunkPosx.startPos
                )
            }.findAny().isPresent) {
            throw BlockPosArgumentType.UNLOADED_EXCEPTION.create()
        }
    }

    data class BlockPlacement(val state: BlockState, val pos: BlockPos, val flag: Int)

    interface StructureTemplateExt {
        var `satisfying$easing`: Animation.Easing?
        var `satisfying$duration`: Duration?
        val `satisfying$blocks`: MutableList<BlockPlacement>
    }

    fun executePlaceTemplate(
        serverCommandSource: ServerCommandSource,
        identifier: Identifier,
        easing: Animation.Easing,
        duration: Duration,
        blockPos: BlockPos = BlockPos.ofFloored(run {
            val player = serverCommandSource.playerOrThrow
            player.pos.add(0.0, 0.0, 0.0).add(player.bodyDirectionVector.normalize().multiply(10.0))
        }),
        blockRotation: BlockRotation = BlockRotation.NONE,
        blockMirror: BlockMirror = BlockMirror.NONE,
        f: Float = 1f,
        i: Int = 0
    ): Int {
        val serverWorld = serverCommandSource.world
        val structureTemplateManager = serverWorld.structureTemplateManager

        val optional: Optional<StructureTemplate>
        try {
            optional = structureTemplateManager.getTemplate(identifier)
        } catch (var13: InvalidIdentifierException) {
            throw TEMPLATE_INVALID_EXCEPTION.create(identifier)
        }

        if (optional.isEmpty) {
            throw TEMPLATE_INVALID_EXCEPTION.create(identifier)
        } else {
            val structureTemplate = optional.get()
            val spawnPos = blockPos.subtract(Vec3i(structureTemplate.size.x / 2, 0, structureTemplate.size.z / 2))
            throwOnUnloadedPos(
                serverWorld,
                ChunkPos(spawnPos),
                ChunkPos(spawnPos.add(structureTemplate.size))
            )
            val structurePlacementData = StructurePlacementData().setMirror(blockMirror).setRotation(blockRotation)
            if (f < 1.0f) {
                structurePlacementData.clearProcessors().addProcessor(BlockRotStructureProcessor(f))
                    .setRandom(StructureBlockBlockEntity.createRandom(i.toLong()))
            }

            (structureTemplate as StructureTemplateExt).apply {
                `satisfying$easing` = easing
                `satisfying$duration` = duration
            }

            val bl = structureTemplate.place(
                serverWorld,
                spawnPos,
                spawnPos,
                structurePlacementData,
                StructureBlockBlockEntity.createRandom(i.toLong()),
                2
            )
            if (!bl) {
                throw TEMPLATE_FAILED_EXCEPTION.create()
            } else {
                return 1
            }
        }
    }

    fun StructureTemplate.structurePlaceEnd(worldAccess: ServerWorldAccess) {
        val dummy = this as StructureTemplateExt
        val easing = this.`satisfying$easing` ?: return
        val duration = this.`satisfying$duration` ?: return

        val blocks = `satisfying$blocks`.sortedBy { it.pos.y }
        for ((index, blockPlacement) in blocks.withIndex()) {
            val (blockState, blockPos, flag) = blockPlacement
            val startDelay = (index * 10).milliseconds.plus(duration.toKotlinDuration())
            val endDelay = ((blocks.size - index) * 10).milliseconds.plus(duration.toKotlinDuration())
            val animation =
                Animation(0f, 1f, startDelay, easing)
            //ELASTIC_IN_OUT
            val idleDelay = 1.35.seconds
            val blockAnimation = BlockAnimation(
                blockPos, blockState, animation,
                Animation(1f, 1f, idleDelay),
                Animation(1f, 0f, endDelay, Animation.Easing.EXPO_IN_OUT),
                Animation(0f, 0f, idleDelay),
            )
            blockAnimationS2C.sendToAll(blockAnimation)
            mcCoroutineTask(sync = true, client = false, delay = startDelay.div(2)) {
                //worldAccess.playSound(null, blockPos, SoundEvents.BLOCK_STONE_PLACE, SoundCategory.BLOCKS)
            }
            if (index < blocks.size / 8) {
                toClean += infiniteMcCoroutineTask(
                    sync = true,
                    client = false,
                    delay = index.ticks,
                    period = startDelay.plus(idleDelay).plus(idleDelay).plus(endDelay)
                ) {
                    if (MinecraftClient.getInstance().world == null) cancel()
                    worldAccess.playSound(
                        null, blockPos, SoundEvents.BLOCK_HONEY_BLOCK_PLACE, SoundCategory.BLOCKS, 1f,
                        Random.nextDouble(0.8, 1.3).toFloat()
                    )
                }
                toClean += infiniteMcCoroutineTask(
                    sync = true,
                    client = false,
                    delay = startDelay.plus(index.ticks).plus(idleDelay).plus(idleDelay.div(1.5)),
                    period = startDelay.plus(idleDelay).plus(idleDelay).plus(endDelay)
                ) {
                    if (MinecraftClient.getInstance().world == null) cancel()
                    worldAccess.playSound(
                        null, blockPos, SoundEvents.BLOCK_HONEY_BLOCK_BREAK, SoundCategory.BLOCKS, 1f,
                        Random.nextDouble(0.8, 1.3).toFloat()
                    )
                }
            }
        }

        val lastDelay = (blocks.size * 10).milliseconds.plus(duration.toKotlinDuration())

        /*mcCoroutineTask(sync = true, client = false, delay = lastDelay) {
            for ((index, blockPlacement) in blocks.reversed().withIndex()) {
                val (blockState, blockPos, flag) = blockPlacement
                val delay = (index * 10).milliseconds.plus(duration.toKotlinDuration())
                println("Delay: ${delay}")
                val animation = Animation(1f, 0f, delay, Animation.Easing.BOUNCE_IN)
                val blockAnimation = BlockAnimation(blockPos, blockState, animation)
                blockAnimationS2C.sendToAll(blockAnimation)
                mcCoroutineTask(sync = true, client = false, delay = 1.ticks) {
                    worldAccess.setBlockState(blockPos, Blocks.AIR.defaultState, Block.NOTIFY_ALL_AND_REDRAW)
                }
            }
        }*/

        `satisfying$duration` = null
        `satisfying$easing` = null
        `satisfying$blocks`.clear()
    }

    val Entity.bodyDirectionVector: Vec3d
        get() {
            val rotY = Math.toRadians(yaw.toDouble())
            val rotX = Math.toRadians(0.0)
            val xz = cos(rotX)
            return Vec3d(-xz * sin(rotY), -sin(rotX), xz * cos(rotY))
        }

    fun StructureTemplate.structurePlace(
        instance: ServerWorldAccess,
        blockPos: BlockPos,
        blockState: BlockState,
        i: Int,
        original: Operation<Boolean>
    ): Boolean {
        val dummy = this as StructureTemplateExt
        if (this.`satisfying$easing` != null) {
            if (!blockState.isAir) {
                `satisfying$blocks`.add(BlockPlacement(blockState, blockPos, i))
                return original.call(instance, blockPos, Blocks.AIR.defaultState, i)
            }
        }

        return original.call(instance, blockPos, blockState, i)
    }
}
*/