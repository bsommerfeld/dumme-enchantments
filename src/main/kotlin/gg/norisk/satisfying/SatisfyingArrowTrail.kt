/*package gg.norisk.satisfying

import gg.norisk.datatracker.entity.getSyncedData
import gg.norisk.datatracker.entity.setSyncedData
import gg.norisk.enchantments.EnchantmentRegistry.arrowTrail
import gg.norisk.enchantments.EnchantmentUtils.getLevel
import gg.norisk.enchantments.StupidEnchantments.MOD_ID
import net.minecraft.entity.Entity
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.projectile.ProjectileEntity
import net.minecraft.particle.DustParticleEffect
import net.minecraft.world.World
import org.joml.Vector3f
import java.awt.Color

object SatisfyingArrowTrail {
    fun applyTrail(livingEntity: LivingEntity, projectileEntity: ProjectileEntity) {
        val trail = arrowTrail.getLevel((livingEntity).getEquippedStack(EquipmentSlot.MAINHAND)) ?: return
        projectileEntity.hasSatisfyingArrowTrail = true
    }

    fun addParticle(instance: World, d: Double, e: Double, f: Double, g: Double, h: Double, i: Double) {
        instance.addParticle(
            getRainbowDustEffect(),
            true,
            d, e, f,
            g, h, i
        )
    }

    private fun getRainbowDustEffect(): DustParticleEffect {
        // Adjust hue range to limit dark purple and increase brightness slightly
        val timeFactor = (System.currentTimeMillis() % 5000L) / 5000.0f

        // Limit hue to a range (e.g., 0 to 0.8 instead of 0 to 1) to avoid very dark colors at the end
        val hue = timeFactor * 0.8f

        // Adjust brightness to make the colors lighter
        val rgb = Color.HSBtoRGB(hue, 0.8f, 1.0f) // Lower saturation for softer colors, full brightness

        // Extract RGB components and normalize them to 0-1 range
        val r = ((rgb shr 16) and 0xFF) / 255.0f
        val g = ((rgb shr 8) and 0xFF) / 255.0f
        val b = (rgb and 0xFF) / 255.0f

        return DustParticleEffect(Vector3f(r, g, b), 1.0f)
    }


    var Entity.hasSatisfyingArrowTrail: Boolean
        get() = this.getSyncedData<Boolean>("$MOD_ID:hasSatisfyingArrowTrail") ?: false
        set(value) {
            this.setSyncedData("$MOD_ID:hasSatisfyingArrowTrail", value)
        }
}*/
