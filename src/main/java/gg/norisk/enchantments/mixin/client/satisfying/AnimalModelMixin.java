package gg.norisk.enchantments.mixin.client.satisfying;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
//import gg.norisk.enchantments.impl.BalloonEnchantment;
//import gg.norisk.satisfying.SatisfyingExperience;
import gg.norisk.enchantments.Versionless;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
//import net.minecraft.client.render.entity.model.AnimalModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Consumer;

@Mixin(Versionless.class)
public abstract class AnimalModelMixin {
    /*@WrapOperation(
            method = "render",
            at = @At(value = "INVOKE", target = "Ljava/lang/Iterable;forEach(Ljava/util/function/Consumer;)V")
    )
    private void satisfying$OutlineModelPart(Iterable<ModelPart> instance, Consumer<ModelPart> consumer, Operation<Void> original, MatrixStack matrixStack, VertexConsumer vertexConsumer, int i, int j, int k) {
        //SatisfyingExperience.INSTANCE.handleHead(this, instance, consumer, original, matrixStack);
    }*/
}
