package gg.norisk.enchantments.mixin.client.satisfying;


//import gg.norisk.satisfying.SatisfyingExperience;
//import me.jellysquid.mods.sodium.client.render.immediate.model.EntityRenderer;
import gg.norisk.enchantments.Versionless;
import net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Versionless.class)
public abstract class EntityRendererMixin {
    /*@Inject(method = "render", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/immediate/model/EntityRenderer;renderCuboids(Lnet/minecraft/client/util/math/MatrixStack$Entry;Lnet/caffeinemc/mods/sodium/api/vertex/buffer/VertexBufferWriter;[Lme/jellysquid/mods/sodium/client/render/immediate/model/ModelCuboid;III)V"))
    private static void satisfying$renderOutline(MatrixStack matrixStack, VertexBufferWriter writer, ModelPart part, int light, int overlay, int color, CallbackInfo ci) {
        //SatisfyingExperience.INSTANCE.renderBlock(part, matrixStack, MinecraftClient.getInstance().player, MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(false));
    }*/
}

