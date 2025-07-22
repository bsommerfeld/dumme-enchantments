package gg.norisk.enchantments.sound

import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.client.sound.MovingSoundInstance
import net.minecraft.client.sound.SoundInstance
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.MathHelper
import java.util.function.Supplier

class SchleuderSoundInstance(
    private val player: ClientPlayerEntity,
    private val condition: Supplier<Boolean>
) :
    MovingSoundInstance(SoundEvents.ITEM_ELYTRA_FLYING, SoundCategory.PLAYERS, SoundInstance.createRandom()) {
    private var tickCount = 0

    init {
        this.repeat = true
        this.repeatDelay = 0
        this.volume = 0.1f
    }

    override fun tick() {
        ++this.tickCount
        val vehicle = player.vehicle
        this.x = player.x.toFloat().toDouble()
        this.y = player.y.toFloat().toDouble()
        this.z = player.z.toFloat().toDouble()
        val f = (player.vehicle?.velocity?.lengthSquared()?.toFloat() ?: 0.1f) * 0.2f
        if (f.toDouble() >= 1.0E-7) {
            this.volume = MathHelper.clamp(f / 6.0f, 0.0f, 0.5f)
        } else {
            this.volume = 0.0f
        }

        if (vehicle != null) {
            if (!player.isRemoved && (!vehicle.isOnGround)) {
                if (this.volume > 0.8f) {
                    this.pitch = 1.0f + (this.volume - 0.8f)
                } else {
                    this.pitch = 1.0f
                }
            } else {
                if (vehicle.isOnGround) {
                    this.setDone()
                }
            }
        }
    }
}
