package gg.norisk.enchantments.mixin;

import gg.norisk.enchantments.impl.BouncyEnchantment;
//import gg.norisk.satisfying.SatisfyingArrowTrail;
//import gg.norisk.satisfying.SatisfyingChainReaction;
import gg.norisk.satisfying.SatisfyingCircleShoot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.BowItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BowItem.class)
public abstract class BowItemMixin {
    @Inject(method = "shoot", at = @At("HEAD"))
    private void shootInjection(LivingEntity livingEntity, ProjectileEntity projectileEntity, int i, float f, float g, float h, LivingEntity livingEntity2, CallbackInfo ci) {
        BouncyEnchantment.INSTANCE.applyBounce(livingEntity, projectileEntity);
        //SatisfyingArrowTrail.INSTANCE.applyTrail(livingEntity, projectileEntity);
        //SatisfyingChainReaction.INSTANCE.applyChainReaction(livingEntity, projectileEntity);
        SatisfyingCircleShoot.INSTANCE.applyCircleShoot(livingEntity, projectileEntity, i, f, g, h);
    }
}
