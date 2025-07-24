package gg.norisk.enchantments.mixin.whiper;

import gg.norisk.enchantments.impl.whiper.WhiperEnchantment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.render.DefaultFramebufferSet;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {
    @Shadow
    @Final
    private MinecraftClient client;
    @Unique
    private static final Identifier RAINDROP = Identifier.ofVanilla("raindrop");

    @Inject(method = "getTransparencyPostEffectProcessor", at = @At("HEAD"), cancellable = true)
    private void nrc$getTransparencyPostEffectProcessor(CallbackInfoReturnable<PostEffectProcessor> cir) {
        if (WhiperEnchantment.INSTANCE.shouldRenderRaindrops()) {
            PostEffectProcessor postEffectProcessor = this.client.getShaderLoader().loadPostEffect(RAINDROP, DefaultFramebufferSet.STAGES);
            cir.setReturnValue(postEffectProcessor);
        }
    }
}
