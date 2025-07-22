package gg.norisk.enchantments.mixin.schleuder;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import gg.norisk.enchantments.EnchantmentRegistry;
import gg.norisk.enchantments.EnchantmentUtils;
import gg.norisk.enchantments.impl.schleuder.SchleuderEnchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BowItem.class)
public abstract class BowItemMixin {
    @Shadow public abstract ActionResult use(World world, PlayerEntity user, Hand hand);

    @Inject(method = "onStoppedUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/BowItem;load(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;)Ljava/util/List;"))
    private void schleuder$onStoppedUsingBow(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfoReturnable<Boolean> cir) {
        SchleuderEnchantment.INSTANCE.onStoppedUsing(stack, world, user, remainingUseTicks, cir);
    }

    @WrapWithCondition(method = "onStoppedUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;playSound(Lnet/minecraft/entity/Entity;DDDLnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FF)V"))
    private boolean schleuder$modifySound(World instance, Entity source, double x, double y, double z, SoundEvent sound, SoundCategory category, float volume, float pitch, ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (EnchantmentUtils.INSTANCE.getLevel(EnchantmentRegistry.INSTANCE.getSchleuder(), stack) != null) {
            return false;
        }
        return true;
    }
}
