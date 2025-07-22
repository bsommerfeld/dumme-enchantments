package gg.norisk.enchantments.mixin.freeze;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import gg.norisk.enchantments.EnchantmentRegistry;
import gg.norisk.enchantments.EnchantmentUtils;
import gg.norisk.enchantments.impl.freeze.FreezeEnchantment;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Inject(method = "getAttackKnockbackAgainst", at = @At("HEAD"), cancellable = true)
    private void getAttackKnockbackAgainstMixin(Entity target, DamageSource damageSource, CallbackInfoReturnable<Float> cir) {
        FreezeEnchantment.INSTANCE.getAttackKnockbackAgainstMixin((LivingEntity) (Object)this, target, damageSource, cir);
    }

    @WrapOperation(
            method = "damage",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;takeKnockback(DDD)V")
    )
    private void stupid$freezeKnockback(LivingEntity instance, double strength, double x, double z, Operation<Void> original, ServerWorld world, DamageSource source, float amount) {
        FreezeEnchantment.INSTANCE.cancelKnockback(instance, strength, x, z, original, world, source, amount);
    }

    @WrapOperation(method = "travelMidAir", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;getSlipperiness()F"))
    private float freeze$redirectSlipperiness(Block instance, Operation<Float> original) {
        return FreezeEnchantment.INSTANCE.apply((LivingEntity) (Object) this, instance, original);
    }
}
