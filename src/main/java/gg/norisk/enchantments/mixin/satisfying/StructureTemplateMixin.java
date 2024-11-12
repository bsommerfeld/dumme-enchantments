package gg.norisk.enchantments.mixin.satisfying;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import gg.norisk.enchantments.utils.Animation;
import gg.norisk.satisfying.SatisfyingBlockAnimation;
import net.minecraft.block.BlockState;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Mixin(StructureTemplate.class)
public abstract class StructureTemplateMixin implements SatisfyingBlockAnimation.StructureTemplateExt {
    @Unique
    private Animation.Easing satisfyingEasing;
    @Unique
    private Duration duration;
    @Unique
    private final List<SatisfyingBlockAnimation.BlockPlacement> blocks = new ArrayList<>();

    @WrapOperation(method = "place", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/ServerWorldAccess;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z", ordinal = 1))
    private boolean satisfying$replacement(ServerWorldAccess instance, BlockPos blockPos, BlockState blockState, int i, Operation<Boolean> original) {
        return SatisfyingBlockAnimation.INSTANCE.structurePlace((StructureTemplate) (Object) this, instance, blockPos, blockState, i, original);
    }

    @Inject(method = "place", at = @At(value = "RETURN"))
    private void satisfying$replacementTail(ServerWorldAccess serverWorldAccess, BlockPos blockPos, BlockPos blockPos2, StructurePlacementData structurePlacementData, Random random, int i, CallbackInfoReturnable<Boolean> cir) {
        SatisfyingBlockAnimation.INSTANCE.structurePlaceEnd((StructureTemplate) (Object) this, serverWorldAccess);
    }

    @Nullable
    @Override
    public Animation.Easing getSatisfying$easing() {
        return satisfyingEasing;
    }

    @Override
    public void setSatisfying$easing(@Nullable Animation.Easing easing) {
        this.satisfyingEasing = easing;
    }

    @Nullable
    @Override
    public Duration getSatisfying$duration() {
        return duration;
    }

    @Override
    public void setSatisfying$duration(@Nullable Duration duration) {
        this.duration = duration;
    }

    @NotNull
    @Override
    public List<SatisfyingBlockAnimation.BlockPlacement> getSatisfying$blocks() {
        return blocks;
    }
}
