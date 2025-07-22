package gg.norisk.enchantments.sound

import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.client.sound.MovingSoundInstance
import net.minecraft.client.sound.SoundInstance
import net.minecraft.sound.SoundCategory
import net.minecraft.util.Hand
import net.minecraft.util.math.MathHelper
import java.util.function.Supplier

class StretchSoundInstance(
    private val player: ClientPlayerEntity,
    private val hand: Hand,
    private val condition: Supplier<Boolean>,
    private val soundEvent: net.minecraft.sound.SoundEvent
) : MovingSoundInstance(soundEvent, SoundCategory.PLAYERS, SoundInstance.createRandom()) {
    
    private var tickCount = 0

    init {
        this.repeat = false
        this.repeatDelay = 0
        this.volume = 0.5f
        this.pitch = 1.0f
    }

    override fun tick() {
        ++this.tickCount
        
        // Update position to player location
        this.x = player.x
        this.y = player.y
        this.z = player.z
        
        // Check if player is still using the item and condition is met
        val isUsingItem = player.isUsingItem && player.activeHand == hand
        
        if (!player.isRemoved && isUsingItem && condition.get()) {
            // Adjust volume based on use time for more realistic stretch effect
            val useTime = player.itemUseTime
            val maxUseTime = player.activeItem.getMaxUseTime(player)
            val useProgress = useTime.toFloat() / maxUseTime.toFloat()
            
            // Volume increases slightly as the item is used longer (stretch gets more intense)
            this.volume = MathHelper.clamp(0.7f + (useProgress * 0.4f), 0.7f, 2f)
            
            // Pitch changes slightly to simulate stretching tension
            this.pitch = MathHelper.clamp(1.0f + (useProgress * 0.2f), 1.0f, 1.2f)
        } else {
            // Stop the sound when item use stops or condition is false
            this.setDone()
        }
    }
} 