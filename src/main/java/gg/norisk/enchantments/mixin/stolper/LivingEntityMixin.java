package gg.norisk.enchantments.mixin.stolper;

import gg.norisk.enchantments.impl.stolper.StolperEnchantment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @ModifyVariable(method = "applyMovementInput", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private Vec3d stumble$applyMovementInput(Vec3d movementInput) {
        if ((Object) this instanceof PlayerEntity player) {
            return StolperEnchantment.INSTANCE.handleApplyMovementInput(player, movementInput);
        }
        return movementInput;
    }
}
