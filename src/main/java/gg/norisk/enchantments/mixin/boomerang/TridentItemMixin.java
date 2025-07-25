package gg.norisk.enchantments.mixin.boomerang;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import gg.norisk.enchantments.impl.boomerang.AxeThrow;
import gg.norisk.enchantments.impl.boomerang.ThrownAxeEntity;
import net.minecraft.component.ComponentType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TridentItem;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.Optional;

@Mixin(TridentItem.class)
public abstract class TridentItemMixin<T extends ProjectileEntity> {

    @Shadow public abstract ProjectileEntity createEntity(World world, Position pos, ItemStack stack, Direction direction);

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
                System.out.println("Stack: " + stackCreate + " / " + stack);
                return ThrownAxeEntity.fromOwnerAndItemStack(world, shooter, stack, finalSlot);
            }
        }, serverWorld, stack, living, roll, power, divergence);
    }

    @WrapOperation(method = "onStoppedUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/EnchantmentHelper;getEffect(Lnet/minecraft/item/ItemStack;Lnet/minecraft/component/ComponentType;)Ljava/util/Optional;"))
    private Optional<RegistryEntry<SoundEvent>> useAxeThrowSound(ItemStack stack, ComponentType<List<RegistryEntry<SoundEvent>>> componentType, Operation<Optional<RegistryEntry<SoundEvent>>> original) {
        if (!AxeThrow.canBeThrown(stack)) {
            //return original.call(stack, componentType);
        }
        //return Optional.ofNullable(AxeThrowSoundEvents.ITEM_THROWN_AXE_THROW);

        return original.call(stack, componentType);
    }
}