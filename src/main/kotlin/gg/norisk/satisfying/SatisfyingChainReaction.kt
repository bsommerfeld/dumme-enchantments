package gg.norisk.satisfying

import gg.norisk.datatracker.entity.getSyncedData
import gg.norisk.datatracker.entity.setSyncedData
import gg.norisk.enchantments.EnchantmentRegistry.chainReaction
import gg.norisk.enchantments.EnchantmentUtils.getLevel
import gg.norisk.enchantments.EnchantmentUtils.sound
import gg.norisk.enchantments.StupidEnchantments.MOD_ID
import gg.norisk.satisfying.SatisfyingArrowTrail.hasSatisfyingArrowTrail
import net.minecraft.entity.Entity
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.projectile.ArrowEntity
import net.minecraft.entity.projectile.ProjectileEntity
import net.minecraft.item.Items
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.Vec3d
import net.silkmc.silk.core.event.EntityEvents

object SatisfyingChainReaction {
    private const val SEARCH_RADIUS = 20.0  // Radius to search for the next target in blocks

    fun initServer() {
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
                    lastHitEntity.sound(SoundEvents.ENTITY_ARROW_HIT_PLAYER, 1f, 1f)
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

        // Find the next closest target within the SEARCH_RADIUS, excluding already hit entities and the shooter
        val nextTarget = findNearestTarget(world, currentTarget, shooter, arrowEntity, hitEntities)
            ?: return  // Stop recursion if no target is found

        // Launch a new arrow toward the next target
        launchArrowAtTarget(world, currentTarget, nextTarget, arrowEntity, shooter)

        // Recursively trigger the next chain reaction from the newly hit target
        // triggerChainReaction(world, nextTarget, shooter, hitEntities)
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
        val direction = Vec3d(to.x - from.x, to.y + to.standingEyeHeight - from.y, to.z - from.z).normalize()
        val newArrow =
            ArrowEntity(world, from.x, from.y + from.standingEyeHeight / 2, from.z, Items.ARROW.defaultStack, null)

        newArrow.owner = shooter ?: from
        newArrow.satisfyingChainReactionOwnerId = arrowEntity.satisfyingChainReactionOwnerId
        newArrow.satisfyingLastChainReactionHitId = from.id
        newArrow.hasSatisfyingArrowTrail = arrowEntity.hasSatisfyingArrowTrail
        newArrow.setVelocity(direction.x, direction.y, direction.z, 1.5f, 0.0f)  // Adjust speed as needed

        world.spawnEntity(newArrow)
    }
}
