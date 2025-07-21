package gg.norisk.satisfying

import gg.norisk.enchantments.EnchantmentRegistry.circleShoot
import gg.norisk.enchantments.EnchantmentUtils.getLevel
import gg.norisk.enchantments.impl.BouncyEnchantment.isBouncy
//import gg.norisk.satisfying.SatisfyingArrowTrail.hasSatisfyingArrowTrail
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.projectile.ArrowEntity
import net.minecraft.entity.projectile.ProjectileEntity
import net.minecraft.item.Items
import net.minecraft.server.world.ServerWorld

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
        when (level) {
            2 -> {
                applySpiralShoot(livingEntity, projectileEntity, i, f, g, h)
                return
            }

            3 -> {
                applyFanShoot(livingEntity, projectileEntity, i, f, g, h)
                return
            }
        }

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
            //newArrow.hasSatisfyingArrowTrail = projectileEntity.hasSatisfyingArrowTrail

            // Spawne den Pfeil in der Welt
            world.spawnEntity(newArrow)
        }
    }

    fun applyFanShoot(
        livingEntity: LivingEntity,
        projectileEntity: ProjectileEntity,
        i: Int,
        f: Float,
        g: Float,
        h: Float
    ) {
        val level = circleShoot.getLevel(livingEntity.getEquippedStack(EquipmentSlot.MAINHAND)) ?: return
        val world = livingEntity.world as? ServerWorld ?: return
        val arrowCount = 16
        val shooterPos = livingEntity.pos
        val initialYaw = livingEntity.yaw
        val initialPitch = livingEntity.pitch

        // Fächerwinkel einstellen (z.B. 45 Grad)
        val spreadAngle = 45.0f

        for (index in 0 until arrowCount) {
            // Berechnet den Yaw-Winkel für jeden Pfeil innerhalb des Fächerwinkels
            val angleOffset = (index - arrowCount / 2) * (spreadAngle / arrowCount)
            val fanYaw = initialYaw + angleOffset

            val newArrow = ArrowEntity(
                world,
                shooterPos.x,
                shooterPos.y + livingEntity.standingEyeHeight,
                shooterPos.z,
                Items.ARROW.defaultStack,
                null
            )
            newArrow.owner = livingEntity

            // Setzt die Pfeilgeschwindigkeit für die Fächerform
            newArrow.setVelocity(livingEntity, initialPitch, fanYaw, 0.0f, f, g)

            // Übertrage besondere Eigenschaften
            newArrow.isBouncy = projectileEntity.isBouncy
            //newArrow.hasSatisfyingArrowTrail = projectileEntity.hasSatisfyingArrowTrail

            world.spawnEntity(newArrow)
        }
    }


    fun applySpiralShoot(
        livingEntity: LivingEntity,
        projectileEntity: ProjectileEntity,
        i: Int,
        f: Float,
        g: Float,
        h: Float
    ) {
        val level = circleShoot.getLevel(livingEntity.getEquippedStack(EquipmentSlot.MAINHAND)) ?: return
        val world = livingEntity.world as? ServerWorld ?: return
        val arrowCount = 32
        val shooterPos = livingEntity.pos
        val initialYaw = livingEntity.yaw
        val initialPitch = livingEntity.pitch

        // Spiralradius und Schrittabstand festlegen
        var radius = 0.5f

        for (index in 0 until arrowCount) {
            val angle = (2 * Math.PI * index) / arrowCount
            val circleYaw = initialYaw + Math.toDegrees(angle).toFloat()

            // Setze den neuen Radius für eine Spirale
            radius += 0.1f  // erhöht den Radius pro Pfeil

            val newArrow = ArrowEntity(
                world,
                shooterPos.x,
                shooterPos.y + livingEntity.standingEyeHeight,
                shooterPos.z,
                Items.ARROW.defaultStack,
                null
            )
            newArrow.owner = livingEntity

            // Setzt Geschwindigkeit basierend auf Abstand vom Mittelpunkt (für Spiralwirkung)
            newArrow.setVelocity(livingEntity, initialPitch, circleYaw, 0.0f, f * radius, g)

            // Übertrage besondere Eigenschaften
            newArrow.isBouncy = projectileEntity.isBouncy
            //newArrow.hasSatisfyingArrowTrail = projectileEntity.hasSatisfyingArrowTrail

            world.spawnEntity(newArrow)
        }
    }
}

