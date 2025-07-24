package gg.norisk.enchantments

import gg.norisk.enchantments.StupidEnchantments.MOD_ID
import net.minecraft.enchantment.Enchantment
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.util.Identifier

object EnchantmentRegistry {
    val fastFalling: RegistryKey<Enchantment> = of("fast_falling")
    val squish: RegistryKey<Enchantment> = of("squish")
    val freeze: RegistryKey<Enchantment> = of("freeze")
    val dopamin: RegistryKey<Enchantment> = of("dopamin")
    val glitch: RegistryKey<Enchantment> = of("glitch")
    val slots: RegistryKey<Enchantment> = of("slots")
    val hot: RegistryKey<Enchantment> = of("hot")
    val slippery: RegistryKey<Enchantment> = of("slippery")
    val bouncy: RegistryKey<Enchantment> = of("bouncy")
    val verification: RegistryKey<Enchantment> = of("verification")
    val helicopter: RegistryKey<Enchantment> = of("helicopter")
    val helicopterV2: RegistryKey<Enchantment> = of("helicopterv2")
    val trashEnchantment: RegistryKey<Enchantment> = of("trash")
    val colossal: RegistryKey<Enchantment> = of("colossal")
    val rolling: RegistryKey<Enchantment> = of("rolling")
    val medusa: RegistryKey<Enchantment> = of("medusa")
    val balloon: RegistryKey<Enchantment> = of("balloon")
    val aimbot: RegistryKey<Enchantment> = of("aimbot")
    val inverted: RegistryKey<Enchantment> = of("inverted")
    val ram: RegistryKey<Enchantment> = of("ram")
    val meme: RegistryKey<Enchantment> = of("meme")
    val experience: RegistryKey<Enchantment> = of("experience")
    val crush: RegistryKey<Enchantment> = of("crush")
    val chainReaction: RegistryKey<Enchantment> = of("chainreaction")
    val arrowTrail: RegistryKey<Enchantment> = of("arrowtrail")
    val circleShoot: RegistryKey<Enchantment> = of("circleshoot")
    val multiFishing: RegistryKey<Enchantment> = of("multifishing")
    val instantFishing: RegistryKey<Enchantment> = of("instantfishing")
    val animation: RegistryKey<Enchantment> = of("animation")
    val schleuder: RegistryKey<Enchantment> = of("schleuder")
    val fork: RegistryKey<Enchantment> = of("fork")
    val raindrop: RegistryKey<Enchantment> = of("raindrop")
    val whiper: RegistryKey<Enchantment> = of("whiper")

    private fun of(name: String): RegistryKey<Enchantment> {
        return RegistryKey.of(RegistryKeys.ENCHANTMENT, Identifier.of(MOD_ID, name))
    }

    fun initialize() {
    }
}
