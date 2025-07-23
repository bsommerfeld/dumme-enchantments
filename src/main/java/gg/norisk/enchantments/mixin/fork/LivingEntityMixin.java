package gg.norisk.enchantments.mixin.fork;

import gg.norisk.enchantments.impl.fork.ForkEnchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    @Shadow
    protected ItemStack activeItemStack;

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "onTrackedDataSet", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getMaxUseTime(Lnet/minecraft/entity/LivingEntity;)I"))
    private void fork$onTrackedDataSet(TrackedData<?> data, CallbackInfo ci) {
        //ForkEnchantment.INSTANCE.onItemStart((LivingEntity) (Object) this, this.activeItemStack);
    }

    @Inject(method = "tickActiveItemStack", at = @At("HEAD"))
    private void fork$tickActiveItemStack(CallbackInfo ci) {
        ForkEnchantment.INSTANCE.tickItemUsage((LivingEntity) (Object) this);
    }
}
