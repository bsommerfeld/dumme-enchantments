package gg.norisk.enchantments.mixin.client.satisfying;

import gg.norisk.satisfying.SatisfyingExperience;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelPart.class)
public abstract class ModelPartMixin implements SatisfyingExperience.ModelPartExt {
    @Unique
    private String name;

    @Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;III)V", at = @At(value = "HEAD"))
    private void satisfying$renderOutline(MatrixStack matrixStack, VertexConsumer vertexConsumer, int i, int j, int k, CallbackInfo ci) {
        //SatisfyingExperience.INSTANCE.renderBlock((ModelPart) (Object) this, matrixStack, MinecraftClient.getInstance().player, MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(false));
    }

    @NotNull
    @Override
    public String getSatisfying$name() {
        return name;
    }

    @Override
    public void setSatisfying$name(@NotNull String s) {
        this.name = s;
    }
}
