package gg.norisk.satisfying

import com.mojang.authlib.GameProfile
import gg.norisk.emote.ext.playEmote
import gg.norisk.enchantments.StupidEnchantments.toId
import gg.norisk.enchantments.sound.SoundRegistry
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.AbstractClientPlayerEntity
import net.minecraft.client.network.OtherClientPlayerEntity
import net.minecraft.client.network.PlayerListEntry
import net.minecraft.client.util.SkinTextures
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerModelPart
import net.minecraft.sound.SoundCategory
import net.silkmc.silk.commands.clientCommand
import net.silkmc.silk.commands.player
import net.silkmc.silk.core.kotlin.ticks
import net.silkmc.silk.core.task.mcCoroutineTask
import org.spongepowered.asm.mixin.injection.invoke.arg.Args
import java.awt.Color
import java.util.*
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

object SatisfyingTrail {
    fun initClient() {
        if (!FabricLoader.getInstance().isDevelopmentEnvironment) return
        clientCommand("satisfyingnichtausführen") {
            literal("trail") {
                argument<Long>("howOften") { howOften ->
                    argument<Double>("period") { period ->
                        argument<Double>("delay") { delay ->
                            argument<Double>("fadeDuration") { fadeDuration ->
                                runs {
                                    spawnAfterImages(
                                        this.source.player,
                                        howOften(),
                                        period().seconds,
                                        delay().seconds,
                                        fadeDuration().seconds
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun spawnAfterImage(base: AbstractClientPlayerEntity, tickDelta: Float, fadeDuration: Duration) {
        val copy = AfterImagePlayer(
            base.clientWorld, GameProfile(UUID.randomUUID(), base.gameProfile.name),
            base.age + tickDelta,
            base.limbAnimator.speed,
            base.limbAnimator.pos,
            base.headYaw,
            base.bodyYaw,
            base.pitch, false
        )
        copy.setPosition(base.pos)
        copy.yaw = base.yaw
        copy.headYaw = base.headYaw
        copy.bodyYaw = base.bodyYaw
        copy.pitch = base.pitch
        copy.handSwingProgress = base.getHandSwingProgress(tickDelta)
        copy.resetFadeTime(fadeDuration)
        mcCoroutineTask(sync = true, client = true, delay = 1.ticks) {
            base.clientWorld.addEntity(copy)
        }
    }

    private fun spawnAfterImages(
        base: AbstractClientPlayerEntity,
        howOften: Long,
        period: Duration,
        delay: Duration,
        fadeDuration: Duration
    ) {
        mcCoroutineTask(sync = true, client = true, howOften = howOften, period = period, delay = delay) {
            val tickDelta = MinecraftClient.getInstance().renderTickCounter.getTickDelta(false)
            spawnAfterImage(base, tickDelta, fadeDuration)
        }
    }

    fun onTick(entity: Entity) {
        if (entity is AfterImagePlayer) {
            entity.noClip = true
        }
    }

    fun <T> modifyAlpha(
        args: Args,
        livingEntity: T,
        f: Float,
        g: Float
    ) {
        val player = livingEntity as? AfterImagePlayer? ?: return
        args.set(4, Color(255, 255, 255, player.getFadeValue()).rgb)
    }

    open class AfterImagePlayer(
        clientWorld: ClientWorld,
        gameProfile: GameProfile,
        val copiedAnimationProgress: Float,
        val copiedLimbSpeed: Float,
        val copiedLimbPos: Float,
        val copiedHeadYaw: Float,
        val copiedBodyYaw: Float,
        val copiedPitch: Float,
        var canFade: Boolean = true,
    ) : OtherClientPlayerEntity(
        clientWorld,
        gameProfile,
    ) {
        var fakeSkinTextures: SkinTextures? = null
        var fadeStart = System.currentTimeMillis()
        var fadeEnd = System.currentTimeMillis()
        var isFallen = false

        override fun isInvisibleTo(playerEntity: PlayerEntity?): Boolean {
            return true
        }

        override fun tick() {
            super.tick()
            if (getFadeValue() <= 0) {
                discard()
            }
        }

        fun getFadeValue(): Int {
            if (!canFade) return 255

            val currentTime = System.currentTimeMillis()
            val fadeDuration = fadeEnd - fadeStart

            if (fadeDuration <= 0) {
                return 0 // Wenn die Fade-Zeit null oder negativ ist, geben wir direkt 0 zurück
            }

            val timeLeft = fadeEnd - currentTime
            val fadeProgress = timeLeft.toFloat() / fadeDuration.toFloat()

            return (fadeProgress * 255).toInt().coerceIn(0, 255)
        }

        fun resetFadeTime(duration: Duration) {
            fadeStart = System.currentTimeMillis()
            this.fadeEnd = fadeStart + duration.inWholeMilliseconds
        }

        override fun getSkinTextures(): SkinTextures {
            return fakeSkinTextures ?: super.getSkinTextures()
        }

        override fun isPartVisible(modelPart: PlayerModelPart?): Boolean {
            return true
        }

        fun getNextDominoPieces(): List<AfterImagePlayer> {
            return clientWorld.getOtherEntities(this, this.boundingBox.expand(1.3)) {
                it is AfterImagePlayer && !it.canFade
            }.filterIsInstance<AfterImagePlayer>().filter { !it.isFallen }
        }

        fun fallNextPiece() {
            val nextPieces = getNextDominoPieces().sortedBy { it.distanceTo(this) }
            //println("NextPieces: ${nextPieces.size}")
            for ((index, nextDominoPiece) in nextPieces.withIndex()) {
                //println("Piece: $nextDominoPiece")
                nextDominoPiece.dominoFall(index == 0)
                return
            }
        }

        fun dominoFall(triggerNext: Boolean = true) {
            isFallen = true
            playEmote("emotes/domino.animation.json".toId())
            mcCoroutineTask(sync = true, client = true, delay = 0.06.seconds) {
                clientWorld.playSoundFromEntity(
                    MinecraftClient.getInstance().player,
                    this@AfterImagePlayer,
                    SoundRegistry.DOMINO_FALL,
                    SoundCategory.PLAYERS,
                    1f,
                    Random.nextDouble(0.9, 1.2).toFloat()
                )
            }
            if (triggerNext) {
                mcCoroutineTask(sync = true, client = true, delay = 0.08.seconds) {
                    fallNextPiece()
                }
            }
        }

        override fun handleAttack(entity: Entity): Boolean {
            if (!canFade) {
                dominoFall()
            }
            return super.handleAttack(entity)
        }

        override fun getPlayerListEntry(): PlayerListEntry? {
            return null
        }
    }
}
