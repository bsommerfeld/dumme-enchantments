package gg.norisk.enchantments.mixin.freeze;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import gg.norisk.enchantments.impl.freeze.FreezeEnchantment;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.mob.PathAwareEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MeleeAttackGoal.class)
public abstract class MeleeAttackGoalMixin {
    @Shadow
    @Final
    protected PathAwareEntity mob;

    @ModifyReturnValue(
            method = "canAttack",
            at = @At("RETURN")
    )
    private boolean canAttackMixin(boolean original) {
        if (FreezeEnchantment.INSTANCE.getNrc_isFrozen(this.mob)) {
            return false;
        }
        return original;
    }
}
