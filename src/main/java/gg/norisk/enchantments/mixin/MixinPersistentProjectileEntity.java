package gg.norisk.enchantments.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
//import gg.norisk.satisfying.SatisfyingArrowTrail;
//import gg.norisk.satisfying.SatisfyingTrail;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PersistentProjectileEntity.class)
public class MixinPersistentProjectileEntity {
    /*@ModifyExpressionValue(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/PersistentProjectileEntity;isCritical()Z")
    )
    private boolean satisfying$arrowTrail(boolean original) {
        return SatisfyingArrowTrail.INSTANCE.getHasSatisfyingArrowTrail((Entity) (Object) this) || original;
    }

    @WrapOperation(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)V")
    )
    private void satisfying$arrowTrailTick(World instance, ParticleEffect particleEffect, double d, double e, double f, double g, double h, double i, Operation<Void> original) {
        if (SatisfyingArrowTrail.INSTANCE.getHasSatisfyingArrowTrail((Entity) (Object) this)) {
            SatisfyingArrowTrail.INSTANCE.addParticle(instance, d, e, f, g, h, i);
        } else {
            original.call(instance, particleEffect, d, e, f, g, h, i);
        }
    }*/
}
