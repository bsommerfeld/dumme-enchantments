package gg.norisk.satisfying

import gg.norisk.datatracker.entity.getSyncedData
import gg.norisk.datatracker.entity.setSyncedData
import gg.norisk.enchantments.EnchantmentRegistry.arrowTrail
import gg.norisk.enchantments.EnchantmentUtils.getLevel
import gg.norisk.enchantments.StupidEnchantments.MOD_ID
import net.minecraft.entity.Entity
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.projectile.ProjectileEntity

object SatisfyingArrowTrail {
    fun applyTrail(livingEntity: LivingEntity, projectileEntity: ProjectileEntity) {
        val trail = arrowTrail.getLevel((livingEntity).getEquippedStack(EquipmentSlot.MAINHAND)) ?: return
        projectileEntity.hasSatisfyingArrowTrail = true
    }

    var Entity.hasSatisfyingArrowTrail: Boolean
        get() = this.getSyncedData<Boolean>("$MOD_ID:hasSatisfyingArrowTrail") ?: false
        set(value) {
            this.setSyncedData("$MOD_ID:hasSatisfyingArrowTrail", value)
        }
}
