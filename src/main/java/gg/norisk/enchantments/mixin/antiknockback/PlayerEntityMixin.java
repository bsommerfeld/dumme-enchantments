package gg.norisk.enchantments.mixin.antiknockback;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import gg.norisk.enchantments.EnchantmentRegistry;
import gg.norisk.enchantments.EnchantmentUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
    @WrapOperation(
            method = "attack",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;takeKnockback(DDD)V")
    )
    private void antiknockback$invertedKnockback(LivingEntity instance, double strength, double x, double z, Operation<Void> original) {
        if ((PlayerEntity) (Object) this instanceof PlayerEntity player && EnchantmentUtils.INSTANCE.getLevel(EnchantmentRegistry.INSTANCE.getAntiknockback(), player.getMainHandStack()) != null) {
            return;
        }
        original.call(instance, strength, x, z);
    }
}
