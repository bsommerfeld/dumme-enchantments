/*package gg.norisk.satisfying

import gg.norisk.datatracker.entity.getSyncedData
import gg.norisk.datatracker.entity.setSyncedData
import gg.norisk.enchantments.EnchantmentRegistry.chainReaction
import gg.norisk.enchantments.EnchantmentUtils.getLevel
import gg.norisk.enchantments.EnchantmentUtils.sound
import gg.norisk.enchantments.StupidEnchantments.MOD_ID
import gg.norisk.enchantments.sound.SoundRegistry
import gg.norisk.satisfying.SatisfyingArrowTrail.hasSatisfyingArrowTrail
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.entity.Entity
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.projectile.ArrowEntity
import net.minecraft.entity.projectile.ProjectileEntity
import net.minecraft.item.Items
import net.minecraft.particle.ParticleEffect
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.Vec3d
import net.silkmc.silk.core.event.EntityEvents

object SatisfyingChainReaction {
    private const val SEARCH_RADIUS = 30.0  // Radius to search for the next target in blocks
    val particles = mutableListOf<Pair<Long, Vec3d>>()

    fun initServer() {
        ServerTickEvents.END_WORLD_TICK.register {
            for ((index, pair) in particles.withIndex()) {
                renderParticleLine(
                    pair.second,
                    particles.getOrNull(index + 1)?.second ?: continue,
                    it
                )
            }
            particles.removeIf { it.first < System.currentTimeMillis() - 5000 }
        }
        EntityEvents.damageLivingEntity.listen { event ->
            val attacker = event.source.attacker
            val source = event.source.source
            val world = event.entity.world as? ServerWorld ?: return@listen

            if (source is ArrowEntity) {
                if (source.satisfyingChainReactionOwnerId == -1) return@listen
                if (source.satisfyingLastChainReactionHitId == -1) {
                    source.satisfyingLastChainReactionHitId = event.entity.id
                }

                val launchedEntity = world.getEntityById(source.satisfyingChainReactionOwnerId)
                val lastHitEntity = world.getEntityById(source.satisfyingLastChainReactionHitId)
                if (lastHitEntity != null) {
                    world.playSound(
                        null,
                        lastHitEntity.pos.x,
                        lastHitEntity.pos.y,
                        lastHitEntity.pos.z,
                        SoundRegistry.ELECTRICITY,
                        SoundCategory.NEUTRAL,
                        1f,
                        2f
                    )
                    //lastHitEntity.sound(SoundEvents.ENTITY_ARROW_HIT_PLAYER, 1f, 1f)
                }

                triggerChainReaction(world, event.entity, launchedEntity, source)
            }
        }
    }

    fun applyChainReaction(livingEntity: LivingEntity, projectileEntity: ProjectileEntity) {
        val reaction = chainReaction.getLevel((livingEntity).getEquippedStack(EquipmentSlot.MAINHAND)) ?: return
        projectileEntity.satisfyingChainReactionOwnerId = livingEntity.id
    }

    var Entity.satisfyingChainReactionOwnerId: Int
        get() = this.getSyncedData<Int>("$MOD_ID:SatisfyingChainReactionOwnerId") ?: -1
        set(value) {
            this.setSyncedData("$MOD_ID:SatisfyingChainReactionOwnerId", value)
        }
    var Entity.satisfyingLastChainReactionHitId: Int
        get() = this.getSyncedData<Int>("$MOD_ID:satisfyingLastChainReactionHitId") ?: -1
        set(value) {
            this.setSyncedData("$MOD_ID:satisfyingLastChainReactionHitId", value)
        }

    private fun triggerChainReaction(
        world: ServerWorld,
        currentTarget: Entity,
        shooter: Entity?,
        arrowEntity: ArrowEntity,
        hitEntities: MutableSet<Entity> = mutableSetOf()
    ) {
        // Mark the current target as hit
        hitEntities.add(currentTarget)
        if (currentTarget !is PlayerEntity) {
            currentTarget.kill()
        }
        particles.add(Pair(System.currentTimeMillis(), arrowEntity.pos))

        // Find the next closest target within the SEARCH_RADIUS, excluding already hit entities and the shooter
        val nextTarget = findNearestTarget(world, currentTarget, shooter, arrowEntity, hitEntities)
            ?: return  // Stop recursion if no target is found

        // Launch a new arrow toward the next target
        launchArrowAtTarget(world, currentTarget, nextTarget, arrowEntity, shooter)

        // Recursively trigger the next chain reaction from the newly hit target
        // triggerChainReaction(world, nextTarget, shooter, hitEntities)
    }

    fun renderParticleLine(
        start: Vec3d,
        end: Vec3d,
        world: ServerWorld,
        particleEffect: ParticleEffect = ParticleTypes.ELECTRIC_SPARK, //ParticleTypes.FLAME,
        particleDensity: Double = 0.2,
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
            world.spawnParticles(
                particleEffect, particleX, particleY, particleZ, 1, 0.0, 0.0, 0.0, 0.0
            )

            // Increment the current distance by the fixed amount
            currentDistance += particleDensity
        }
    }


    private fun findNearestTarget(
        world: ServerWorld,
        origin: Entity,
        shooter: Entity?,
        arrowEntity: ArrowEntity,
        hitEntities: Set<Entity>
    ): LivingEntity? {
        // Get nearby entities within SEARCH_RADIUS
        return world.getEntitiesByClass(
            LivingEntity::class.java,
            origin.boundingBox.expand(SEARCH_RADIUS)
        ) { entity ->
            entity.isAlive && entity != origin && entity != shooter && entity !in hitEntities && arrowEntity.satisfyingChainReactionOwnerId != entity.id
                    && arrowEntity.satisfyingLastChainReactionHitId != entity.id
        }.minByOrNull { origin.squaredDistanceTo(it) }  // Find the closest valid entity
    }

    private fun launchArrowAtTarget(
        world: ServerWorld,
        from: Entity,
        to: Entity,
        arrowEntity: ArrowEntity,
        shooter: Entity?
    ) {
        // Calculate the direction vector from `from` to `to`
        val direction = Vec3d(to.x - from.x, to.y + to.standingEyeHeight / 2 - from.y, to.z - from.z).normalize()

        // Calculate distance between the two entities
        val distance = from.pos.distanceTo(to.pos).toFloat()

        // Set a speed multiplier based on distance, with a slight increase
        val baseSpeed = 1.5f
        val speedMultiplier =
            1.0f + if (distance > 15) distance * 0.05f else (distance * 0.005f)  // Adjust 0.05f to control the speed increase per unit distance

        // Create and configure the new arrow entity
        val newArrow =
            ArrowEntity(world, from.x, from.y + from.standingEyeHeight / 2, from.z, Items.ARROW.defaultStack, null)
        newArrow.owner = shooter ?: from
        newArrow.satisfyingChainReactionOwnerId = arrowEntity.satisfyingChainReactionOwnerId
        newArrow.satisfyingLastChainReactionHitId = from.id
        newArrow.hasSatisfyingArrowTrail = arrowEntity.hasSatisfyingArrowTrail

        // Set the velocity with the calculated speed multiplier
        newArrow.setVelocity(direction.x, direction.y, direction.z, baseSpeed * speedMultiplier, 0.0f)

        // Spawn the arrow in the world
        world.spawnEntity(newArrow)
    }
}
*/