package gg.norisk.enchantments

import gg.norisk.enchantments.command.EnchantmentsCommand
import gg.norisk.enchantments.command.SatisfyingCommand
import gg.norisk.enchantments.impl.*
import gg.norisk.enchantments.impl.freeze.FreezeEnchantment
import gg.norisk.enchantments.sound.SoundRegistry
import gg.norisk.satisfying.*
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.ModInitializer
import net.minecraft.util.Identifier
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object StupidEnchantments : ModInitializer, ClientModInitializer {
    const val MOD_ID = "enchantments"
    val logger: Logger = LogManager.getLogger(MOD_ID)
    fun String.toId() = Identifier.of(MOD_ID, this)

    override fun onInitialize() {
        logger.info("Helloooo")
        EnchantmentRegistry.initialize()
        SoundRegistry.init()
        //DopaminEnchantment.initServer()
        //ColossalEnchantment.initServer()
        //HotEnchantment.initServer()
        HelicopterEnchantment.initServer()
        HelicopterEnchantmentV2.initServer()
        FreezeEnchantment.initServer()
        //TrashEnchantment.initServer()
        EnchantmentsCommand.initServer()
        SatisfyingCommand.initServer()
        //MedusaEnchantment.initServer()
        //BalloonEnchantment.initServer()
        AimBotEnchantment.initServer()
        //RamEnchantment.initServer()
        //SatisfyingExperience.initServer()
        //SatisfyingChainReaction.initServer()
        //SatisfyingSuperStar.initServer()
        //SatisfyingBlockAnimation.initServer()
        //SatisfyingTrail.initServer()
    }

    override fun onInitializeClient() {
        logger.info("Helloooo Client")
        //SatisfyingSuperStar.initClient()
        //SquishEnchantment.initClient()
        //RollEnchantment.initClient()
        //TrashEnchantment.initClient()
        //DopaminEnchantment.initClient()
        GlitchEnchantment.initClient()
        //SlipperyEnchantment.initClient()
        VerificationEnchantment.initClient()
        HelicopterEnchantment.initClient()
        HelicopterEnchantmentV2.initClient()
        FreezeEnchantment.initClient()
        //MemeEnchantment.initClient()
        //SatisfyingTrail.initClient()
        //SatisfyingExperience.initClient()
        //SatisfyingCrush.initClient()
        //SatisfyingBlockAnimation.initClient()
    }
}
