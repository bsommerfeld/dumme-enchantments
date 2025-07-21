package gg.norisk.enchantments.mixin.client.satisfying;

import gg.norisk.enchantments.impl.HelicopterEnchantmentV2;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin {
    @Inject(method = "setupTransforms(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;FF)V", at = @At(value = "HEAD"))
    private void enchantments$helicopterRenderingV2(PlayerEntityRenderState playerEntityRenderState, MatrixStack matrixStack, float f, float g, CallbackInfo ci) {
        HelicopterEnchantmentV2.INSTANCE.handleRotationRendering(playerEntityRenderState, matrixStack, f, g);
    }

    @Inject(method = "updateRenderState(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;F)V", at = @At(value = "HEAD"))
    private void enchantments$fixBody(AbstractClientPlayerEntity abstractClientPlayerEntity, PlayerEntityRenderState playerEntityRenderState, float f, CallbackInfo ci) {
        HelicopterEnchantmentV2.INSTANCE.handleFixBody(abstractClientPlayerEntity);
    }
}
