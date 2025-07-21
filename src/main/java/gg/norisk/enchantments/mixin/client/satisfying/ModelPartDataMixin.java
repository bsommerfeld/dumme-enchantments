package gg.norisk.enchantments.mixin.client.satisfying;


import com.llamalad7.mixinextras.sugar.Local;
//import gg.norisk.satisfying.SatisfyingExperience;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ModelPartData.class)
public abstract class ModelPartDataMixin {
    /*@Inject(method = "createPart", at = @At("TAIL"))
    private void satisfying$createPart(int i, int j, CallbackInfoReturnable<ModelPart> cir, @Local Object2ObjectArrayMap<String, ModelPart> map) {
        map.forEach((name, modelPart) -> {
            ((SatisfyingExperience.ModelPartExt) (Object) modelPart).setSatisfying$name(name);
        });
    }*/
}
