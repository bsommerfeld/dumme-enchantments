package gg.norisk.enchantments.sound

import gg.norisk.enchantments.impl.whiper.WhiperEnchantment.nrc_hasWhiper
import kotlinx.coroutines.withTimeoutOrNull
import net.minecraft.client.MinecraftClient
import net.minecraft.client.sound.MovingSoundInstance
import net.minecraft.client.sound.SoundInstance
import net.minecraft.entity.Entity
import net.minecraft.sound.SoundCategory

class WhiperSoundInstance(
    val entity: Entity,
) : MovingSoundInstance(
    SoundRegistry.WHIPER, SoundCategory.PLAYERS,
    SoundInstance.createRandom()
) {
    private var tickCount = 0

    init {
        this.repeat = true
        this.repeatDelay = 0
        this.volume = 1f
    }

    fun stop() {
        this.setDone()
    }

    override fun tick() {
        tickCount++
        if (!entity.isRemoved && entity.nrc_hasWhiper) {
            this.x = entity.x.toFloat().toDouble()
            this.y = entity.y.toFloat().toDouble()
            this.z = entity.z.toFloat().toDouble()
        } else {
            this.setDone()
        }
    }
}
