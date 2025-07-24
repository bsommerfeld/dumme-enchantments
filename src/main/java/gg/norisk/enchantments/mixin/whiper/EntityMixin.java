package gg.norisk.enchantments.mixin.whiper;

import gg.norisk.enchantments.impl.whiper.WhiperEnchantment;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "setSneaking", at = @At("TAIL"))
    private void nrc$setSneaking(boolean sneaking, CallbackInfo ci) {
        WhiperEnchantment.INSTANCE.toggleSneaking((Entity) (Object) this, sneaking);
    }
}
