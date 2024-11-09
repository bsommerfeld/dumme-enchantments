package gg.norisk.enchantments.mixin.satisfying;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import gg.norisk.satisfying.SatisfyingFishing;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FishingBobberEntity.class)
public abstract class FishingBobberEntityMixin extends Entity {
    @Shadow
    private boolean inOpenWater;

    @Shadow
    private int fishTravelCountdown;

    @Shadow
    private float fishAngle;

    public FishingBobberEntityMixin(EntityType<?> entityType, World world) {
        super(entityType, world);
    }

    @ModifyConstant(method = "tickFishingLogic", constant = @Constant(intValue = 1, ordinal = 0))
    private int satisfying$cooldown(int value) {
        if (SatisfyingFishing.INSTANCE.getFishingHookOwnerId(this) != -1) {
            return 200;
        } else {
            return value;
        }
    }

    @Inject(
            method = "tickFishingLogic",
            at = @At(value = "HEAD")
    )
    private void satisfying$tickFishingLogicHead(BlockPos blockPos, CallbackInfo ci) {
        var parent = getWorld().getEntityById(SatisfyingFishing.INSTANCE.getSatisfyingBobberId(this));
        if (parent instanceof FishingBobberEntity bobber) {
            fishTravelCountdown = ((FishingBobberEntityAccessor) bobber).getFishTravelCountdown();
            fishAngle = ((FishingBobberEntityAccessor) bobber).getFishAngle();
        }
    }

    @Inject(
            method = "tickFishingLogic",
            at = @At(value = "FIELD", target = "Lnet/minecraft/entity/projectile/FishingBobberEntity;fishTravelCountdown:I", ordinal = 2)
    )
    private void satisfying$travelTime(BlockPos blockPos, CallbackInfo ci, @Local LocalIntRef color) {
        if (SatisfyingFishing.INSTANCE.getFishingHookOwnerId(this) != -1) {
            color.set(1);
        }
    }
}
