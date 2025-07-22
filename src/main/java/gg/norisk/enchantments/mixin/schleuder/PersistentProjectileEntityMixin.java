package gg.norisk.enchantments.mixin.schleuder;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import gg.norisk.enchantments.impl.BouncyEnchantment;
import gg.norisk.enchantments.impl.schleuder.SchleuderEnchantment;
import gg.norisk.enchantments.sound.SoundRegistry;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.sound.SoundEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PersistentProjectileEntity.class)
public abstract class PersistentProjectileEntityMixin {
    @WrapOperation(
            method = "onBlockHit",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/PersistentProjectileEntity;playSound(Lnet/minecraft/sound/SoundEvent;FF)V")
    )
    private void bypassExpensiveCalculationIfNecessary(PersistentProjectileEntity instance, SoundEvent soundEvent, float pitch, float volume, Operation<Void> original) {
        var isSchleuder = SchleuderEnchantment.INSTANCE.getNrc_isSchleuderArrow(instance);
        if (isSchleuder != null && isSchleuder && !BouncyEnchantment.INSTANCE.isBouncy(instance)) {
            original.call(instance, SoundRegistry.INSTANCE.getBONK(), volume, 1f);
        } else {
            original.call(instance, soundEvent, volume, pitch);
        }
    }
}
