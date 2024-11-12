package gg.norisk.enchantments.mixin.client.satisfying;

import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin {
    @Inject(method = "method_41930", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;setBlockBreakingInfo(ILnet/minecraft/util/math/BlockPos;I)V", shift = At.Shift.AFTER))
    private void satisfying$setBlockBreakingInfo(BlockState blockState, BlockPos blockPos, Direction direction, int i, CallbackInfoReturnable<Packet> cir) {
    }
}
