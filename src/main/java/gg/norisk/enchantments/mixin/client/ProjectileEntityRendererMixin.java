package gg.norisk.enchantments.mixin.client;

import net.minecraft.client.render.entity.ProjectileEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ProjectileEntityRenderer.class)
public abstract class ProjectileEntityRendererMixin {
    /*protected ProjectileEntityRendererMixin(EntityRendererFactory.Context context) {
        super(context);
    }

    @Inject(method = "render(Lnet/minecraft/entity/projectile/PersistentProjectileEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("HEAD"), cancellable = true)
    private void stupid$aimbotRenderer(T persistentProjectileEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        if (AimBotEnchantment.INSTANCE.handleRendering((ProjectileEntityRenderer<T>) (Object) this, persistentProjectileEntity, f, g, matrixStack, vertexConsumerProvider, i)) {
            super.render(persistentProjectileEntity, f, g, matrixStack, vertexConsumerProvider, i);
            ci.cancel();
        }
    } */
}
