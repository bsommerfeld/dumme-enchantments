package gg.norisk.animations

import gg.norisk.emote.network.EmoteNetworking.playEmote
import gg.norisk.enchantments.StupidEnchantments.toId
import net.minecraft.server.network.ServerPlayerEntity

/**
 * Animation facade for Stolper (stumble) enchantment.
 * Encapsulates triggering of the stumble emote.
 */
object StolperAnimation {
    private val EMOTE_ID = "emotes/stolpernv2.animation.json".toId()

    fun playStumbleEmote(player: ServerPlayerEntity) {
        player.playEmote(EMOTE_ID)
    }
}
