package gg.norisk.enchantments.mixin.client;

import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity> {
    /*@Inject(method = "render", at = @At("HEAD"))
    private void renderInjection(T entity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
    }

    @Shadow
    @Final
    protected EntityRenderDispatcher dispatcher;

    @Shadow
    @Final
    private TextRenderer textRenderer;

    @Inject(method = "render", at = @At(value = "HEAD"))
    private void injected(T entity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        AimBotEnchantment.INSTANCE.renderTargetNameTag(entity, f, g, matrixStack, vertexConsumerProvider, i, dispatcher, textRenderer);
    }*/
}
