package gg.norisk.enchantments.mixin.camerashaker;

import gg.norisk.enchantments.utils.CameraShaker;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow
    protected abstract void moveBy(float f, float g, float h);

    @Inject(
            method = "update",
            at = @At(
                    // Inject before the call to clipToSpace
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/Camera;setPos(DDD)V",
                    shift = At.Shift.BY,
                    by = 1
            )
    )
    void camerashake$onUpdate(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        double x = CameraShaker.INSTANCE.getAvgX();
        double y = CameraShaker.INSTANCE.getAvgY();
        moveBy((float) .0, (float) y, (float) x);
    }
}
