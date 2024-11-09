package gg.norisk.enchantments.mixin.satisfying;

import net.minecraft.entity.projectile.FishingBobberEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FishingBobberEntity.class)
public interface FishingBobberEntityAccessor {
    @Accessor("fishTravelCountdown")
    int getFishTravelCountdown();

    @Accessor("fishAngle")
    float getFishAngle();
}
