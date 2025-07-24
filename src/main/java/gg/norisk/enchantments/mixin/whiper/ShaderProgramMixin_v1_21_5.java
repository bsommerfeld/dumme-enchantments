package gg.norisk.enchantments.mixin.whiper;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexFormat;
import gg.norisk.enchantments.impl.whiper.WhiperEnchantment;
import gg.norisk.utils.CustomTextShaderUniforms;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.ShaderProgram;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ShaderProgram.class)
public abstract class ShaderProgramMixin_v1_21_5 {
    @Shadow
    public abstract @Nullable GlUniform getUniform(String name);

    @Shadow
    @Final
    private int glRef;
    @Unique
    GlUniform whiperActive;

    @Inject(method = "set", at = @At("TAIL"))
    private void nrc$setCustomUniforms(List<RenderPipeline.UniformDescription> uniforms, List<String> samplers, CallbackInfo ci) {
        whiperActive = this.getUniform("WiperActive");
    }

    @Inject(method = "initializeUniforms", at = @At(value = "TAIL"))
    private void nrc$initializeUniforms(VertexFormat.DrawMode drawMode, Matrix4f viewMatrix, Matrix4f projectionMatrix, float screenWidth, float screenHeight, CallbackInfo ci) {
        if (this.whiperActive != null) {
            var player = MinecraftClient.getInstance().player;
            var isActive = 0f;
            if (player != null) {
                if (WhiperEnchantment.INSTANCE.getNrc_hasWhiper(player)) {
                    isActive = 1f;
                }
            }
            this.whiperActive.set(isActive);
        }
    }
}