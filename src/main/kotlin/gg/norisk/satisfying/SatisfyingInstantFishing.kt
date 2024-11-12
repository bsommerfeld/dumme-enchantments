package gg.norisk.satisfying

import gg.norisk.datatracker.entity.getSyncedData
import gg.norisk.datatracker.entity.setSyncedData
import gg.norisk.enchantments.EnchantmentRegistry.instantFishing
import gg.norisk.enchantments.EnchantmentUtils.getLevel
import gg.norisk.enchantments.StupidEnchantments.MOD_ID
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.projectile.FishingBobberEntity
import net.minecraft.util.Hand
import net.minecraft.world.World

object SatisfyingInstantFishing {
    fun mark(
        playerEntity: PlayerEntity,
        world: World,
        i: Int,
        j: Int,
        entity: FishingBobberEntity
    ) {
        val mainStack = playerEntity.getStackInHand(Hand.MAIN_HAND)
        //println("MainStack $mainStack ${playerEntity.activeHand}")
        instantFishing.getLevel(mainStack) ?: return
        entity.isInstantFishing = true
    }

    var Entity.isInstantFishing: Boolean
        get() = this.getSyncedData<Boolean>("$MOD_ID:isInstantFishing") ?: false
        set(value) {
            this.setSyncedData("$MOD_ID:isInstantFishing", value)
        }
}
