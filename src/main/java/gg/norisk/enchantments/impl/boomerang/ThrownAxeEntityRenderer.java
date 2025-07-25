package gg.norisk.enchantments.impl.boomerang;

import gg.norisk.enchantments.utils.Animation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RotationAxis;

public class ThrownAxeEntityRenderer extends EntityRenderer<ThrownAxeEntity, ThrownAxeEntityRenderState> {
    public ThrownAxeEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(ThrownAxeEntityRenderState state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();
        matrices.translate(0.0, 0.25f, 0.0);
        matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(90.0F - state.entity.getYaw()));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0F));
        //matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0F));
        var tickDelta = MinecraftClient.getInstance().getRenderTickCounter().getTickProgress(false);
        matrices.multiply(RotationAxis.NEGATIVE_Z.rotationDegrees(state.entity.getTicksActiveLerped(tickDelta)));
        //noinspection UnstableApiUsage
        ItemStack stack = state.entity.getAttachedOrElse(AxeThrowAttachments.THROWN_AXE_ITEM_STACK, state.entity.getItemStack()).copy();
        if (state.entity.isEnchanted()) {
            stack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        }
        MinecraftClient.getInstance().getItemRenderer().renderItem(stack, ItemDisplayContext.FIXED, light, OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, state.entity.getWorld(), 0);
        matrices.pop();
        super.render(state, matrices, vertexConsumers, light);
    }

    @Override
    public boolean shouldRender(ThrownAxeEntity entity, Frustum frustum, double x, double y, double z) {
        return true;
    }

    @Override
    public ThrownAxeEntityRenderState createRenderState() {
        return new ThrownAxeEntityRenderState();
    }

    @Override
    public void updateRenderState(ThrownAxeEntity entity, ThrownAxeEntityRenderState state, float tickDelta) {
        super.updateRenderState(entity, state, tickDelta);
        state.entity = entity;
    }
}