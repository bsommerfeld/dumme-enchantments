package gg.norisk.enchantments.mixin.satisfying;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import gg.norisk.enchantments.EnchantmentRegistry;
import gg.norisk.enchantments.EnchantmentUtils;
import gg.norisk.satisfying.SatisfyingFishing;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FishingRodItem.class)
public abstract class FishingRodItemMixin extends Item {
    public FishingRodItemMixin(Settings settings) {
        super(settings);
    }

    @Inject(method = "use", at = @At(value = "HEAD", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"), cancellable = true)
    private void satisfying$moreFishesHead(World world, PlayerEntity playerEntity, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        ItemStack itemStack = playerEntity.getStackInHand(hand);
        SatisfyingFishing.INSTANCE.useFish(world, playerEntity, hand, itemStack, cir);
    }

    @Inject(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z", shift = At.Shift.AFTER))
    private void satisfying$moreFishes(World world, PlayerEntity playerEntity, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        SatisfyingFishing.INSTANCE.moreFishes(world, playerEntity, hand);
    }

    @WrapWithCondition(
            method = "use",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z")
    )
    private boolean satisfying$cancelFish(World instance, Entity entity, World world, PlayerEntity playerEntity, Hand hand) {
        var itemStack = playerEntity.getStackInHand(hand);
        return EnchantmentUtils.INSTANCE.getLevel(EnchantmentRegistry.INSTANCE.getMultiFishing(), itemStack) == null;
    }
}
