package gg.norisk.enchantments.mixin.satisfying;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import gg.norisk.satisfying.SatisfyingExperience;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExperienceOrbEntity.class)
public abstract class ExperienceOrbEntityMixin extends Entity {
    public ExperienceOrbEntityMixin(EntityType<?> entityType, World world) {
        super(entityType, world);
    }

    @Shadow
    protected abstract int repairPlayerGears(ServerPlayerEntity serverPlayerEntity, int i);

    @Shadow
    private int amount;

    @Shadow
    private int pickingCount;

    @Shadow
    private PlayerEntity target;

    @Shadow private int orbAge;

    @ModifyReturnValue(
            method = "getGravity",
            at = @At("RETURN")
    )
    private double satisfying$getGravity(double original) {
        if (SatisfyingExperience.INSTANCE.isCustom((ExperienceOrbEntity) (Object) this)) {
            return 0;
        }
        return original;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void satisfying$tick(CallbackInfo ci) {
        if (SatisfyingExperience.INSTANCE.isCustom((ExperienceOrbEntity) (Object) this)) {
            this.orbAge += 60;
        }
    }

    @Unique
    private void pickUp(PlayerEntity playerEntity) {
        if (playerEntity instanceof ServerPlayerEntity serverPlayerEntity) {
            playerEntity.sendPickup(this, 1);
            int i = this.repairPlayerGears(serverPlayerEntity, this.amount);
            if (i > 0) {
                playerEntity.addExperience(i);
            }

            this.pickingCount--;
            if (this.pickingCount == 0) {
                this.discard();
            }
        }
    }

    @ModifyConstant(method = "onPlayerCollision", constant = @Constant(intValue = 2))
    private int satisfying$onPlayerCollision(int constant) {
        if (SatisfyingExperience.INSTANCE.isCustom((ExperienceOrbEntity) (Object) this)) {
            return 0;
        }
        return constant;
    }

    @ModifyConstant(method = "expensiveUpdate", constant = @Constant(doubleValue = 8.0))
    private double satisfying$expensiveUpdate(double constant) {
        if (SatisfyingExperience.INSTANCE.isCustom((ExperienceOrbEntity) (Object) this)) {
            return 2.5;
        }
        return constant;
    }

    @WrapWithCondition(
            method = "expensiveUpdate",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ExperienceOrbEntity;merge(Lnet/minecraft/entity/ExperienceOrbEntity;)V")
    )
    private boolean satisfying$cancelMerge(ExperienceOrbEntity instance, ExperienceOrbEntity experienceOrbEntity) {
        return !SatisfyingExperience.INSTANCE.isCustom((ExperienceOrbEntity) (Object) this);
    }
}
