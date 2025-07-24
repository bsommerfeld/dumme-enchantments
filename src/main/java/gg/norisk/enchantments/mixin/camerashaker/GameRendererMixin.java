package gg.norisk.enchantments.mixin.camerashaker;

import gg.norisk.enchantments.utils.CameraShaker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(
            method = "render",
            at = @At("HEAD")
    )
    private void heroapi$onRender(RenderTickCounter renderTickCounter, boolean tick, CallbackInfo ci) {
        if (!client.skipGameRender && tick && client.world != null) {
            CameraShaker.INSTANCE.newFrame();
        }
    }

    @Inject(
            method = "renderHand",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/GameRenderer;tiltViewWhenHurt(Lnet/minecraft/client/util/math/MatrixStack;F)V"
            )
    )
    private void heroapi$shakeHand(Camera camera, float f, Matrix4f matrix4f, CallbackInfo ci) {
        float x = (float) CameraShaker.INSTANCE.getAvgX();
        float y = (float) CameraShaker.INSTANCE.getAvgY();

        matrix4f.translate(x, -y, (float) .0); // opposite of camera
    }
}
