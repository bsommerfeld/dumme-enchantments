package gg.norisk.enchantments.sound

import gg.norisk.enchantments.impl.HelicopterEnchantmentV2.helicopterMovement
import net.minecraft.client.sound.MovingSoundInstance
import net.minecraft.client.sound.SoundInstance
import net.minecraft.entity.Entity
import net.minecraft.sound.SoundCategory
import net.minecraft.util.math.MathHelper

class HelicopterSoundInstanceV2(
    val entity: Entity,
) : MovingSoundInstance(SoundRegistry.HELICOPTER, SoundCategory.PLAYERS, SoundInstance.createRandom()) {
    private var tickCount = 0

    init {
        this.repeat = true
        this.repeatDelay = 0
        this.volume = 0.2f // Start leise
        this.pitch = 0.4f // Start tief
    }

    fun stop() {
        this.setDone()
    }

    override fun tick() {
        tickCount++
        val movement = entity.helicopterMovement

        if (!entity.isRemoved && (movement.isFlying || movement.rotationPower > 0)) {
            // Position updaten
            this.x = entity.x.toFloat().toDouble()
            this.y = entity.y.toFloat().toDouble()
            this.z = entity.z.toFloat().toDouble()
            
            // Volume basierend auf Rotationsgeschwindigkeit (0.0 - 1.0)
            val rotationSpeed = movement.getCurrentRotationSpeed()
            val maxSpeed = 15f // Max rotation speed
            val volumeIntensity = (rotationSpeed / maxSpeed).coerceIn(0f, 1f)
            this.volume = MathHelper.lerp(0.05f, this.volume, volumeIntensity * 1.2f) // Max 80% volume
            
            // Pitch basierend auf Power/Speed (realistischer Motor-Sound)
            val targetPitch = 0.8f + (volumeIntensity * 0.4f) // 0.8 - 1.2 pitch range
            this.pitch = MathHelper.lerp(0.1f, this.pitch, targetPitch)
            
        } else {
            // Fade out wenn gestoppt
            this.volume = MathHelper.lerp(0.1f, this.volume, 0f)
            if (this.volume < 0.01f) {
                this.setDone()
            }
        }
    }
} 