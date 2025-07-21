/*package gg.norisk.satisfying

import gg.norisk.datatracker.entity.getSyncedData
import gg.norisk.datatracker.entity.setSyncedData
import gg.norisk.enchantments.EnchantmentRegistry.multiFishing
import gg.norisk.enchantments.EnchantmentUtils.getLevel
import gg.norisk.enchantments.StupidEnchantments.MOD_ID
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.projectile.FishingBobberEntity
import net.minecraft.item.ItemStack
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import kotlin.math.cos
import kotlin.math.sin

object SatisfyingFishing {
    fun moreFishes(
        world: World,
        playerEntity: PlayerEntity,
        hand: Hand,
    ) {
        val itemStack = playerEntity.getStackInHand(hand)
        val level = multiFishing.getLevel(itemStack) ?: return
        if (world is ServerWorld) {
            // Abstand zwischen den einzelnen Bobbern in der Reihe (quer zur Blickrichtung)
            val offsetDistance = 0.5 // Abstand in Blöcken

            // Startrichtung und Position des Spielers
            val playerPos = playerEntity.eyePos
            val playerYaw = Math.toRadians(playerEntity.yaw.toDouble())

            // Berechne die Quer-Richtung zur Blickrichtung des Spielers für die horizontale Linie
            val offsetX = cos(playerYaw) * offsetDistance
            val offsetZ = sin(playerYaw) * offsetDistance

            var first: FishingBobberEntity? = null

            for (i in -level..level) { // Generiert 10 Bobber-Entitäten quer zur Blickrichtung
                val j = (EnchantmentHelper.getFishingTimeReduction(world, itemStack, playerEntity) * 20.0f).toInt()
                val k = EnchantmentHelper.getFishingLuckBonus(world, itemStack, playerEntity)

                // Berechne die Position für den aktuellen Bobber, indem der Offset multipliziert wird
                val bobberPos = playerPos.add(offsetX * i, 0.0, offsetZ * i)

                // Erstelle und positioniere einen neuen FishingBobberEntity
                val bobber = FishingBobberEntity(playerEntity, world, k, j)
                bobber.updatePosition(bobberPos.x, bobberPos.y, bobberPos.z)

                bobber.fishingHookOwnerId = playerEntity.id

                if (first == null) {
                    first = bobber
                } else {
                    bobber.satisfyingBobberId = first.id
                }

                // Spawn die Bobber-Entity in der Welt
                world.spawnEntity(bobber)
            }
        }
    }

    fun useFish(
        world: World,
        playerEntity: PlayerEntity,
        hand: Hand,
        itemStack: ItemStack,
        cir: CallbackInfoReturnable<TypedActionResult<ItemStack>>
    ) {
        val serverWorld = world as? ServerWorld? ?: return
        val list = serverWorld.iterateEntities().filterIsInstance<FishingBobberEntity>()
            .filter { it.fishingHookOwnerId == playerEntity.id }.onEach {
                it.use(itemStack)
            }
        if (list.isNotEmpty()) {
            cir.returnValue = TypedActionResult.success(itemStack, world.isClient())
        }
    }

    var Entity.fishingHookOwnerId: Int
        get() = this.getSyncedData<Int>("$MOD_ID:fishingHookOwnerId") ?: -1
        set(value) {
            this.setSyncedData("$MOD_ID:fishingHookOwnerId", value)
        }

    var Entity.satisfyingBobberId: Int
        get() = this.getSyncedData<Int>("$MOD_ID:satisfyingBobberId") ?: -1
        set(value) {
            this.setSyncedData("$MOD_ID:satisfyingBobberId", value)
        }
}
*/