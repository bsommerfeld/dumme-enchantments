package gg.norisk.enchantments.impl.freeze

import gg.norisk.enchantments.impl.freeze.FreezeEnchantment.nrc_frozenAnimation
import gg.norisk.utils.ext.EntityRenderStateExt
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.feature.FeatureRenderer
import net.minecraft.client.render.entity.feature.FeatureRendererContext
import net.minecraft.client.render.entity.model.EntityModel
import net.minecraft.client.render.entity.state.EntityRenderState
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import java.awt.Color

@Environment(EnvType.CLIENT)
class FreezeOverlayFeatureRenderer<S : EntityRenderState, M : EntityModel<S>>(featureRendererContext: FeatureRendererContext<S, M>) :
    FeatureRenderer<S, M>(featureRendererContext) {
    override fun render(
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
        state: S,
        limbAngle: Float,
        limbDistance: Float
    ) {
        val entity = (state as? EntityRenderStateExt?)?.nrc_entity
        val animation = entity?.nrc_frozenAnimation
        if (animation != null) {
            matrices.push()
            //matrices.scale(1f,0.5f,1f)
            val f = state.age
            val entityModel = this.contextModel
            val vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(getEnergySwirlTexture()))
            entityModel.setAngles(state)
            val color = Color(255, 255, 255, animation.get().toInt())
            entityModel.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, color.rgb)
            matrices.pop()
        }
    }

    fun getEnergySwirlX(partialAge: Float): Float {
        return 0f
    }

    private fun getEnergySwirlTexture(): Identifier {
        return Identifier.of("enchantments", "textures/freeze_overlay.png")
    }

    companion object {
        fun initClient() {

        }
    }
}
