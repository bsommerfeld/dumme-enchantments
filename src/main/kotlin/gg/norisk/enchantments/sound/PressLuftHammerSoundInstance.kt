package gg.norisk.enchantments.sound

import gg.norisk.enchantments.impl.presslufthammer.PressLuftHammer.nrc_isPressLuftHammer
import net.minecraft.client.sound.MovingSoundInstance
import net.minecraft.client.sound.SoundInstance
import net.minecraft.entity.Entity
import net.minecraft.sound.SoundCategory

class PressLuftHammerSoundInstance(private val entity: Entity) :
    MovingSoundInstance(SoundRegistry.JACKHAMMER, SoundCategory.PLAYERS, SoundInstance.createRandom()) {
    var fadeTime = 5
    var isFading = false

    init {
        this.repeat = true
        this.repeatDelay = 0
        this.volume = 0.01f
    }

    override fun tick() {
        if (isFading) {
            --fadeTime
            this.volume *= 0.9f
            if (fadeTime < 0) {
                this.setDone()
                return
            }
        }

        this.x = entity.x.toFloat().toDouble()
        this.y = entity.y.toFloat().toDouble()
        this.z = entity.z.toFloat().toDouble()

        if (!entity.isRemoved && entity.nrc_isPressLuftHammer) {
            this.volume = 1f
            this.pitch = 1f
        } else {
            isFading = true
        }
    }
}
