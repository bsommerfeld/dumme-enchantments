package gg.norisk.enchantments.mixin.stolper;

import gg.norisk.enchantments.impl.stolper.StolperEnchantment;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "playStepSounds", at = @At("HEAD"))
    private void stumble$playStepSounds(BlockPos pos, BlockState state, CallbackInfo ci) {
        StolperEnchantment.INSTANCE.onStepSound((Entity) (Object) this, pos, state, ci);
    }
}
