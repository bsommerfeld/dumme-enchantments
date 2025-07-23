package gg.norisk.enchantments.mixin.fork;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import gg.norisk.enchantments.impl.fork.ForkEnchantment;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin {
    @Inject(method = "renderFirstPersonItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemDisplayContext;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", ordinal = 1))
    private void fork$redirectRenderItem(AbstractClientPlayerEntity player, float tickProgress, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        ForkEnchantment.INSTANCE.handleFirstPersonRendering(player, tickProgress, pitch, hand, swingProgress, item, matrices, vertexConsumers, light);
    }

    @ModifyExpressionValue(
            method = "renderFirstPersonItem",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;isUsingItem()Z", ordinal = 1)
    )
    private boolean fork$disableTransformation(boolean original, AbstractClientPlayerEntity player,
                                               float tickProgress,
                                               float pitch,
                                               Hand hand,
                                               float swingProgress,
                                               ItemStack item,
                                               float equipProgress,
                                               MatrixStack matrices,
                                               VertexConsumerProvider vertexConsumers,
                                               int light) {
        if (original) {
            return ForkEnchantment.INSTANCE.handleEatingAnimation((HeldItemRenderer) (Object) this, original, player, tickProgress, pitch, hand, swingProgress, item, equipProgress, matrices, vertexConsumers, light);
        }
        return original;
    }
}
