package gg.norisk.enchantments.mixin.boomerang;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import gg.norisk.enchantments.impl.boomerang.AxeThrow;
import gg.norisk.enchantments.impl.boomerang.ThrownAxeEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TridentItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TridentItem.class)
public abstract class TridentItemMixin<T extends ProjectileEntity> {


    @WrapOperation(method = "onStoppedUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/ProjectileEntity;spawnWithVelocity(Lnet/minecraft/entity/projectile/ProjectileEntity$ProjectileCreator;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;FFF)Lnet/minecraft/entity/projectile/ProjectileEntity;"))
    public T invokeThrownAxeConstructor(ProjectileEntity.ProjectileCreator<T> creator, ServerWorld serverWorld, ItemStack stack, LivingEntity living, float roll, float power, float divergence, Operation<T> original, @Local PlayerEntity player) {
        if (!AxeThrow.canBeThrown(stack)) {
            return original.call(creator, serverWorld, stack, living, roll, power, divergence);
        }
        PlayerInventory inventory = player.getInventory();
        int slot = 0;
        final int size = inventory.size();
        for (int i = 0; i < size; i++) {
            ItemStack tempStack = inventory.getStack(i);
            if (stack.equals(tempStack)) {
                slot = i;
                break;
            }
        }

        int finalSlot = slot;
        return original.call(new ProjectileEntity.ProjectileCreator<>() {
            @Override
            public ProjectileEntity create(ServerWorld world, LivingEntity shooter, ItemStack stackCreate) {
                return ThrownAxeEntity.fromOwnerAndItemStack(world, shooter, stack, finalSlot);
            }
        }, serverWorld, stack, living, roll, power, divergence);
    }

    @WrapOperation(method = "onStoppedUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;playSoundFromEntity(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/Entity;Lnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FF)V"))
    private void useAxeThrowSound(World instance, Entity source, Entity entity, SoundEvent sound, SoundCategory category, float volume, float pitch, Operation<Void> original, ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (AxeThrow.canBeThrown(stack)) {
            return;
        }
        original.call(instance, source, entity, sound, category, volume, pitch);
        //return Optional.ofNullable(AxeThrowSoundEvents.ITEM_THROWN_AXE_THROW);
    }
}