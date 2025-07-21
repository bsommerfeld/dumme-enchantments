package gg.norisk.enchantments.mixin.helicopterv2;

import gg.norisk.enchantments.impl.HelicopterEnchantmentV2;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityModel.class)
public class PlayerEntityModelMixin extends BipedEntityModel<PlayerEntityRenderState> {
    public PlayerEntityModelMixin(ModelPart modelPart) {
        super(modelPart);
    }

    @Inject(method = "setAngles(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;)V", at = @At(value = "TAIL"))
    private void helicopter$setAnglesTail(PlayerEntityRenderState playerEntityRenderState, CallbackInfo ci) {
        HelicopterEnchantmentV2.INSTANCE.handleApplyPlayerModelRotations((PlayerEntityModel) (Object) this, playerEntityRenderState);
    }
}
