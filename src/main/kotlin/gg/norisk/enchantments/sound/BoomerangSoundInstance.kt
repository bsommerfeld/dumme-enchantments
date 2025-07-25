package gg.norisk.enchantments.sound

import net.minecraft.client.sound.MovingSoundInstance
import net.minecraft.client.sound.SoundInstance
import net.minecraft.entity.Entity
import net.minecraft.sound.SoundCategory

class BoomerangSoundInstance(private val entity: Entity) :
    MovingSoundInstance(SoundRegistry.BOOMERANG, SoundCategory.PLAYERS, SoundInstance.createRandom()) {
    var fadeTime = 5
    var isFading = false

    init {
        this.repeat = true
        this.repeatDelay = 0
        this.volume = 1f
    }

    override fun tick() {
        if (isFading) {
            --fadeTime
            this.volume *= 0.4f
            if (fadeTime < 0) {
                this.setDone()
                return
            }
        }

        this.x = entity.x.toFloat().toDouble()
        this.y = entity.y.toFloat().toDouble()
        this.z = entity.z.toFloat().toDouble()

        if (!entity.isRemoved && entity.velocity.horizontalLengthSquared() > 0) {
            this.volume = 1f
            this.pitch = 1f
        } else {
            isFading = true
        }
    }
}
