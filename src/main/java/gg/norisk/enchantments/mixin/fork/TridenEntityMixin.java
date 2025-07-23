package gg.norisk.enchantments.mixin.fork;

import gg.norisk.enchantments.impl.fork.ForkEnchantment;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TridentEntity.class)
public abstract class TridenEntityMixin extends PersistentProjectileEntity {
    protected TridenEntityMixin(EntityType<? extends PersistentProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = {
            "initDataTracker",
    }, at = @At(value = "TAIL"))
    private void injected(DataTracker.Builder builder, CallbackInfo ci) {
        //ForkEnchantment.INSTANCE.onInitDataTracker((TridentEntity) (Object) this);
    }

    @Inject(method = "onEntityHit", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/enchantment/EnchantmentHelper;onTargetDamaged(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/damage/DamageSource;Lnet/minecraft/item/ItemStack;Ljava/util/function/Consumer;)V"
    ))
    private void injected(EntityHitResult entityHitResult, CallbackInfo ci) {
        ForkEnchantment.INSTANCE.onEntityHit((TridentEntity) (Object) this, entityHitResult, ci);
    }
}
