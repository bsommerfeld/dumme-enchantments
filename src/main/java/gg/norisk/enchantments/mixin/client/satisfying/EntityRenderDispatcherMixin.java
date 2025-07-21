package gg.norisk.enchantments.mixin.client.satisfying;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
//import gg.norisk.satisfying.SatisfyingExperience;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;


@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {
    /*@WrapWithCondition(
            method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EntityRenderDispatcher;renderShadow(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/entity/Entity;FFLnet/minecraft/world/WorldView;F)V")
    )
    private boolean satisfying$cancelShadow(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, Entity entity, float f, float g, WorldView worldView, float h) {
        if (entity instanceof ExperienceOrbEntity exp) {
            return !SatisfyingExperience.INSTANCE.isCustom(exp);
        }
        return true;
    }*/
}
