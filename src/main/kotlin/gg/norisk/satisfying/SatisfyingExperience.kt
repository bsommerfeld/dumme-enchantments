package gg.norisk.satisfying

import gg.norisk.datatracker.entity.getSyncedData
import gg.norisk.datatracker.entity.setSyncedData
import gg.norisk.datatracker.entity.syncedValueChangeEvent
import gg.norisk.enchantments.EnchantmentRegistry.experience
import gg.norisk.enchantments.EnchantmentUtils.getLevel
import gg.norisk.enchantments.StupidEnchantments.MOD_ID
import gg.norisk.enchantments.StupidEnchantments.toId
import gg.norisk.enchantments.mixin.client.ModelPartAccessor
import gg.norisk.enchantments.mixin.client.satisfying.CuboidAccessor
import gg.norisk.enchantments.sound.SoundRegistry
import gg.norisk.enchantments.utils.Vec3dSerializer
import kotlinx.serialization.Serializable
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents
import net.fabricmc.loader.impl.lib.sat4j.core.Vec
import net.minecraft.client.MinecraftClient
import net.minecraft.client.model.ModelPart
import net.minecraft.client.model.ModelPart.Cuboid
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.feature.FeatureRenderer
import net.minecraft.client.render.entity.feature.FeatureRendererContext
import net.minecraft.client.render.entity.model.EntityModel
import net.minecraft.client.render.entity.model.EntityModelLayers
import net.minecraft.client.render.item.HeldItemRenderer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.ExperienceOrbEntity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.particle.DustParticleEffect
import net.minecraft.particle.ParticleEffect
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.util.Hand
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.random.Random
import net.silkmc.silk.core.kotlin.ticks
import net.silkmc.silk.core.task.mcCoroutineTask
import net.silkmc.silk.network.packet.c2sPacket
import org.joml.Vector3f

object SatisfyingExperience {
    fun initServer() {
        ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register { world, attacker, killed ->
            val player = attacker as? PlayerEntity? ?: return@register
            val stack = player.getStackInHand(Hand.MAIN_HAND)
            val level = experience.getLevel(stack) ?: return@register
            killed.disableExperienceDropping()
            attacker.lastKilled = killed.id
        }
        positionPacket.receiveOnServer { packet, context ->
            //println("Position")
            mcCoroutineTask(sync = true, client = false) {
                packet.forEach { cuboidCorners ->
                    // Render lines for each cuboid's corner points
                    for (i in cuboidCorners.indices) {
                        // Connect each corner point to the next, and loop back to the start at the end
                        val start = cuboidCorners[i].vec3d
                        val end = cuboidCorners[(i + 1) % cuboidCorners.size].vec3d
                        mcCoroutineTask(delay = i.ticks, sync = true, client = false) {
                            context.player.world.playSound(
                                null,
                                start.x,
                                start.y,
                                start.z,
                                SoundRegistry.EXPERIENCE_LINE,
                                SoundCategory.NEUTRAL,
                                0.1f,//kotlin.random.Random.nextDouble(0.7,1.5).toFloat(),
                                1f
                            )
                            renderXPLine(start, end, context.player.serverWorld)
                        }
                    }
                }
            }
        }
    }

    var Entity.lastKilled: Int
        get() = this.getSyncedData<Int>("$MOD_ID:SatisfyingLastKilled") ?: -1
        set(value) {
            this.setSyncedData("$MOD_ID:SatisfyingLastKilled", value)
        }

    var ExperienceOrbEntity.isCustom: Boolean
        get() = this.getSyncedData<Boolean>("$MOD_ID:SatisfyingIsCustomXp") ?: false
        set(value) {
            this.setSyncedData("$MOD_ID:SatisfyingIsCustomXp", value)
        }


    //fragt nicht...
    @Serializable
    data class Vec3dWrapper(@Serializable(with = Vec3dSerializer::class) val vec3d: Vec3d)

    val positionPacket =
        c2sPacket<List<List<Vec3dWrapper>>>("experience-position-packet".toId())

    fun initClient() {
        syncedValueChangeEvent.listen { event ->
            if (event.entity == MinecraftClient.getInstance().player) return@listen
            if (event.key == "$MOD_ID:SatisfyingLastKilled") {

            }
        }


        LivingEntityFeatureRendererRegistrationCallback.EVENT.register { entityType, entityRenderer, registrationHelper, context ->
            val modelPart = EntityModelLayers.getLayers().toList()
                .firstOrNull { it.id.path == entityType.untranslatedName.lowercase() }
            if (modelPart != null) {
                val model = context.getPart(modelPart)
                registrationHelper.register(
                    BoxFeatureRenderer(
                        entityRenderer as FeatureRendererContext<LivingEntity, EntityModel<LivingEntity>>,
                        context.heldItemRenderer,
                        model
                    )
                )
            }
        }
    }

    interface ModelPartExt {
        var `satisfying$name`: String
    }

    class BoxFeatureRenderer<T : LivingEntity, M : EntityModel<T>>(
        context: FeatureRendererContext<T, M>,
        private val heldItemRenderer: HeldItemRenderer,
        val root: ModelPart,
    ) : FeatureRenderer<T, M>(context) {
        override fun render(
            matrices: MatrixStack,
            vertexConsumers: VertexConsumerProvider,
            light: Int,
            entity: T,
            limbAngle: Float,
            limbDistance: Float,
            tickDelta: Float,
            animationProgress: Float,
            headYaw: Float,
            headPitch: Float
        ) {

            // das modell ist an diesem zeitpunkt schon animiert und positioniert
            var current: Class<*> = this.contextModel::class.java

            while (current.superclass != null) { // we don't want to process Object.class
                current.declaredFields.forEach { field ->
                    runCatching {
                        field.isAccessible = true
                        field.get(this.contextModel) as ModelPart
                    }.onSuccess {
                        val random = Random.create()
                        if (it.isEmpty) return@onSuccess
                        //println("Part: ${(it as ModelPartExt).`satisfying$name`}")
                        it.renderBlock(matrices, entity, tickDelta, vertexConsumers)
                    }
                }
                current = current.superclass
            }
        }
    }

    fun renderXPLine(
        start: Vec3d,
        end: Vec3d,
        world: ServerWorld,
        distanceBetweenParticles: Double = 0.2// Fixed distance between each particle
    ) {
        // Calculate the distance between start and end points
        val totalDistance = start.distanceTo(end)

        // Calculate the direction vector between start and end, normalized
        val directionX = (end.x - start.x) / totalDistance
        val directionY = (end.y - start.y) / totalDistance
        val directionZ = (end.z - start.z) / totalDistance

        // Generate particles at intervals of distanceBetweenParticles
        var currentDistance = 0.0
        while (currentDistance <= totalDistance) {
            // Calculate the current particle position along the line
            val particleX = start.x + directionX * currentDistance
            val particleY = start.y + directionY * currentDistance
            val particleZ = start.z + directionZ * currentDistance

            // Set the position of each custom experience orb entity
            world.spawnEntity(ExperienceOrbEntity(world, particleX, particleY, particleZ, 1)?.apply {
                isCustom = true
                this.setVelocity(0.0,0.0,0.0)

                //this.amount = 1
                setPosition(particleX, particleY, particleZ)
            })

            // Increment the current distance by the fixed amount
            currentDistance += distanceBetweenParticles
        }
    }

    fun renderParticleLine(
        start: Vec3d,
        end: Vec3d,
        world: ServerWorld,
        particleEffect: ParticleEffect = DustParticleEffect(Vector3f(1f, 0f, 0f), 0.1f), //ParticleTypes.FLAME,
        particleDensity: Int = 20,
    ) {
        // Erhalte den Spieler und die Welt

        // Berechne den Abstand zwischen Start und Endpunkt
        val deltaX = end.x - start.x
        val deltaY = end.y - start.y
        val deltaZ = end.z - start.z

        // Partikel entlang der Linie erzeugen
        for (i in 0..particleDensity) {
            // Interpoliert zwischen dem Start- und Endpunkt
            val t = i / particleDensity.toDouble()
            val particleX = start.x + deltaX * t
            val particleY = start.y + deltaY * t
            val particleZ = start.z + deltaZ * t

            // Übersetze die Partikelposition relativ zur Kamera
            val worldParticlePos = Vec3d(
                particleX,
                particleY,
                particleZ,
            )

            // Erstelle einen Partikel-Effekt
            world.spawnParticles(
                particleEffect, worldParticlePos.x, worldParticlePos.y, worldParticlePos.z, 1, 0.0, 0.0, 0.0, 0.0
            )
        }
    }


    fun Cuboid.renderCuboidAgain(
        entry: MatrixStack.Entry,
        vertexConsumer: VertexConsumer,
        i: Int,
        j: Int,
        k: Int,
        entity: LivingEntity,
        tickDelta: Float,
        camDiff: Vector3f
    ): List<Vec3d> {
        val dummy = this as CuboidAccessor
        val matrix4f = entry.positionMatrix
        val vector3f = Vector3f()
        val cornerPoints = mutableListOf<Vec3d>()

        for (quad in this.sides) {
            entry.transformNormal(quad.direction, vector3f)

            for (vertex in quad.vertices) {
                val l = vertex.pos.x() / 16.0f
                val m = vertex.pos.y() / 16.0f
                val n = vertex.pos.z() / 16.0f
                val vector3f3 = matrix4f.transformPosition(l, m, n, vector3f)

                val vec = Vector3f(vector3f3).apply { sub(camDiff) }
                val pos = Vec3d(entity.lastRenderX, entity.lastRenderY, entity.lastRenderZ)
                    .lerp(entity.pos, tickDelta.toDouble())
                    .add(vec.x.toDouble(), vec.y.toDouble(), vec.z.toDouble())

                cornerPoints.add(pos)
            }
        }
        return cornerPoints
    }

    fun <T : LivingEntity> ModelPart.renderBlock(
        matrices: MatrixStack, entity: T, tickDelta: Float, vertexConsumers: VertexConsumerProvider
    ) {
        val dummy = this as ModelPartAccessor
        val supported = listOf(EntityType.PLAYER, EntityType.PIG)
        //if (entity.type !in supported) return
        val name = (this as ModelPartExt).`satisfying$name`
        val byPass = listOf("cloak", "hat", "ear", "left_sleeve", "right_sleeve", "left_pants", "right_pants", "jacket")
        if (byPass.any { it.equals(name, true) }) return
        //if (!name.contains("arm")) return

        // println("${(this as ModelPartExt).`satisfying$name`}")

        matrices.push()
        rotate(matrices)

        val camPos = MinecraftClient.getInstance().gameRenderer.camera.pos
        val renderX = MathHelper.lerp(tickDelta.toDouble(), entity.lastRenderX, entity.x)
        val renderY = MathHelper.lerp(tickDelta.toDouble(), entity.lastRenderY, entity.y)
        val renderZ = MathHelper.lerp(tickDelta.toDouble(), entity.lastRenderZ, entity.z)

        val diffX = renderX - camPos.x
        val diffY = renderY - camPos.y
        val diffZ = renderZ - camPos.z

        val camDiff = Vector3f(diffX.toFloat(), diffY.toFloat(), diffZ.toFloat())
        val positions = mutableListOf<List<Vec3dWrapper>>()

        for (cuboid in this.cuboids) {
            positions += cuboid.renderCuboidAgain(
                matrices.peek(), vertexConsumers.getBuffer(RenderLayer.getSolid()), 1, 1, -1, entity, tickDelta, camDiff
            ).map { Vec3dWrapper(it) }
        }

        if (entity.id == MinecraftClient.getInstance().player?.lastKilled) {
            positionPacket.send(positions)
            entity.discard()
        }

        matrices.pop()
    }
}
