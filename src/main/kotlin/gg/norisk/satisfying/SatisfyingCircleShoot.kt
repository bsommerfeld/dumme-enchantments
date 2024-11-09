package gg.norisk.satisfying

import gg.norisk.enchantments.EnchantmentRegistry.circleShoot
import gg.norisk.enchantments.EnchantmentUtils.getLevel
import gg.norisk.enchantments.impl.BouncyEnchantment.isBouncy
import gg.norisk.satisfying.SatisfyingArrowTrail.hasSatisfyingArrowTrail
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.projectile.ArrowEntity
import net.minecraft.entity.projectile.ProjectileEntity
import net.minecraft.item.Items
import net.minecraft.server.world.ServerWorld
import net.silkmc.silk.core.kotlin.ticks
import net.silkmc.silk.core.task.mcCoroutineTask
import kotlin.math.cos
import kotlin.math.sin

object SatisfyingCircleShoot {
    fun applyCircleShoot(
        livingEntity: LivingEntity,
        projectileEntity: ProjectileEntity,
        i: Int,
        f: Float,
        g: Float,
        h: Float
    ) {
        // Bestimme das Level des Kreis-Schieß-Verzauberung
        val level = circleShoot.getLevel(livingEntity.getEquippedStack(EquipmentSlot.MAINHAND)) ?: return
        val world = livingEntity.world as? ServerWorld ?: return

        // Anzahl der Pfeile im Kreis
        val arrowCount = 32  // Anzahl der Pfeile im Kreis (32 für einen vollen Kreis)

        // Aktuelle Position des Schützen und die Richtung des originalen Projektils
        val shooterPos = livingEntity.pos
        val initialYaw = livingEntity.yaw
        val initialPitch = livingEntity.pitch

        for (index in 0 until arrowCount) {
            // Berechne den Winkel für jede Pfeilrichtung im Kreis (in Bogenmaß)
            val angle = (2 * Math.PI * index) / arrowCount

            // Berechne die Pfeilrichtung basierend auf dem Ursprungspitch und Yaw, aber mit dem Kreiswinkel
            val circleYaw = initialYaw + Math.toDegrees(angle).toFloat()

            // Erstelle einen neuen Pfeil an der Position des Schützen
            val newArrow = ArrowEntity(
                world,
                shooterPos.x,
                shooterPos.y + livingEntity.standingEyeHeight,
                shooterPos.z,
                Items.ARROW.defaultStack,
                null
            )
            newArrow.owner = livingEntity

            // Setze die Geschwindigkeit des Pfeils so, dass er im Kreis fliegt
            newArrow.setVelocity(livingEntity, initialPitch, circleYaw, 0.0f, f, g)

            // Übertrage spezielle Eigenschaften vom Originalprojektil
            newArrow.isBouncy = projectileEntity.isBouncy
            newArrow.hasSatisfyingArrowTrail = projectileEntity.hasSatisfyingArrowTrail

            // Spawne den Pfeil in der Welt
            world.spawnEntity(newArrow)
        }
    }


}

