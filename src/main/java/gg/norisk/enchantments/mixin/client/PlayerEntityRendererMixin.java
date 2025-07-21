package gg.norisk.enchantments.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
//import gg.norisk.enchantments.impl.RollEnchantment;
//import gg.norisk.satisfying.SatisfyingSuperStar;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin {


    /*@Inject(method = "setupTransforms(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/client/util/math/MatrixStack;FFFF)V", at = @At(value = "HEAD"), cancellable = true)
    private void stupid$setupTransformsInjection(AbstractClientPlayerEntity abstractClientPlayerEntity, MatrixStack matrixStack, float f, float g, float h, float i, CallbackInfo ci) {
        RollEnchantment.INSTANCE.handleMatrixStackRotation(abstractClientPlayerEntity, matrixStack, f, g, h, i);
    }

    @WrapOperation(
            method = "renderArm",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/RenderLayer;getEntitySolid(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/RenderLayer;")
    )
    private RenderLayer satisfying$renderArm(Identifier identifier, Operation<RenderLayer> original, MatrixStack matrixStack,
                                             VertexConsumerProvider vertexConsumerProvider,
                                             int i,
                                             AbstractClientPlayerEntity abstractClientPlayerEntity,
                                             ModelPart modelPart,
                                             ModelPart modelPart2) {
        if (SatisfyingSuperStar.INSTANCE.isSatisfyingSuperMario(abstractClientPlayerEntity)) {
            return SatisfyingSuperStar.INSTANCE.getENTITY_SOLID().apply(identifier);
        } else {
            return original.call(identifier);
        }
    }*/
}
