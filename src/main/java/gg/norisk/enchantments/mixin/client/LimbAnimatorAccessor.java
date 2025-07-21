package gg.norisk.enchantments.mixin.client;

import net.minecraft.entity.LimbAnimator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LimbAnimator.class)
public interface LimbAnimatorAccessor {
    /*@Accessor("pos")
    float getPos();

    @Accessor("pos")
    void setPos(float pos);

    @Accessor("prevSpeed")
    float getPrevSpeed();

    @Accessor("prevSpeed")
    void setPrevSpeed(float pos);*/
}
