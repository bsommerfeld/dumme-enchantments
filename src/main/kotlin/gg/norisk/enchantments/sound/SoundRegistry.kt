package gg.norisk.enchantments.sound

import gg.norisk.enchantments.StupidEnchantments.toId
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.sound.SoundEvent

object SoundRegistry {
    var GOOFY_AAH_BEAT =
        Registry.register(Registries.SOUND_EVENT, "goofy_ah_beat".toId(), SoundEvent.of("goofy_ah_beat".toId()))
    var GLITCH =
        Registry.register(Registries.SOUND_EVENT, "glitch".toId(), SoundEvent.of("glitch".toId()))
    var BOUNCY =
        Registry.register(Registries.SOUND_EVENT, "bouncy".toId(), SoundEvent.of("bouncy".toId()))
    var HELICOPTER =
        Registry.register(Registries.SOUND_EVENT, "helicopter".toId(), SoundEvent.of("helicopter".toId()))
    var TRASH_OPEN =
        Registry.register(Registries.SOUND_EVENT, "trash_open".toId(), SoundEvent.of("trash_open".toId()))
    var TRASH_CLOSE =
        Registry.register(Registries.SOUND_EVENT, "trash_close".toId(), SoundEvent.of("trash_close".toId()))
    val BALLOON_POP = register("balloon_pop")
    val BALLOON_BLOW_UP = register("balloon_blow_up")
    val DOMINO_FALL = register("domino_fall")
    val EXPERIENCE_LINE = register("experience_line")
    val HYDRAULIC_PRESS_START = register("hydraulic_press_start")
    val STAR_SOUND = register("star_sound")
    val ELECTRICITY = register("electricity")
    val FREEZE = register("freeze")
    val SLINGSHOT_RELEASE = register("slingshot_release")
    val SLINGTSHOT_STRETCH = register("slingshot_stretch")
    val BONK = register("bonk")
    val WHIPER = register("whiper")
    val LIGHTSWITCH_OFF = register("lightswitch_off")
    val BOOMERANG = register("boomerang")
    val JACKHAMMER = register("jackhammer")

    fun init() {
    }

    private fun register(string: String): SoundEvent {
        return Registry.register(Registries.SOUND_EVENT, string.toId(), SoundEvent.of(string.toId()))
    }
}
