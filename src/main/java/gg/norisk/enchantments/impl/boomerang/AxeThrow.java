package gg.norisk.enchantments.impl.boomerang;

import gg.norisk.enchantments.EnchantmentRegistry;
import gg.norisk.enchantments.EnchantmentUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import survivalblock.axe_throw.common.init.AxeThrowAttachments;
//import survivalblock.axe_throw.common.init.AxeThrowDataComponentTypes;
//import survivalblock.axe_throw.common.init.AxeThrowGameRules;
//import survivalblock.axe_throw.common.init.AxeThrowSoundEvents;

public class AxeThrow implements ModInitializer {

    public static final String MOD_ID = "axe_throw";

    public static boolean throwingAxeAndNotTrident = false;

    public static final Logger LOGGER = LoggerFactory.getLogger("Axe Throw");

    @Override
    public void onInitialize() {
        //AxeThrowDataComponentTypes.init();
        AxeThrowAttachments.init();
        //AxeThrowGameRules.init();
        //AxeThrowSoundEvents.init();
        AxeThrowEntityTypes.init();
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            EntityRendererRegistry.register(AxeThrowEntityTypes.THROWN_AXE, ThrownAxeEntityRenderer::new);
        }
    }

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean canBeThrown(ItemStack stack) {
        return EnchantmentUtils.INSTANCE.getLevel(EnchantmentRegistry.INSTANCE.getBoomerang(), stack) != null;
    }
}