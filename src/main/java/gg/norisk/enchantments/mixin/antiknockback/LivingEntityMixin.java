package gg.norisk.enchantments.mixin.antiknockback;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import gg.norisk.enchantments.EnchantmentRegistry;
import gg.norisk.enchantments.EnchantmentUtils;
import gg.norisk.enchantments.impl.InvertedEnchantment;
import gg.norisk.enchantments.impl.antiknockback.AntiKnockbackEnchantment;
import it.unimi.dsi.fastutil.doubles.DoubleDoubleImmutablePair;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    @Shadow
    protected abstract float getAttackKnockbackAgainst(Entity target, DamageSource damageSource);

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @WrapOperation(
            method = "damage",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;takeKnockback(DDD)V")
    )
    private void stupid$invertedKnockback(LivingEntity instance, double strength, double x, double z, Operation<Void> original, ServerWorld world, DamageSource damageSource, float amount) {
        Entity attacker = damageSource.getAttacker();
        if (attacker instanceof PlayerEntity player && EnchantmentUtils.INSTANCE.getLevel(EnchantmentRegistry.INSTANCE.getAntiknockback(), player.getMainHandStack()) != null) {
            var antikbLevel = EnchantmentUtils.INSTANCE.getLevel(EnchantmentRegistry.INSTANCE.getAntiknockback(), player.getMainHandStack());
            AntiKnockbackEnchantment.INSTANCE.handleAntiKnockback(damageSource, player, instance, original, strength, antikbLevel);
        } else {
            original.call(instance, strength, x, z);
        }
    }
}
