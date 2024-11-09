package gg.norisk.enchantments.mixin.client.satisfying;

import net.minecraft.client.model.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ModelPart.Cuboid.class)
public interface CuboidAccessor {
    @Accessor("sides")
    ModelPart.Quad[] getSides();
}
