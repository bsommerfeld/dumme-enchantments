package gg.norisk.enchantments.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.option.GameOptions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin extends Input {
    @Shadow
    @Final
    private GameOptions settings;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void enchantments$lockInputsWhileStolper(CallbackInfo ci) {
        var client = MinecraftClient.getInstance();
        if (client == null) return;
        var player = client.player;
        if (player == null) return;
        if (gg.norisk.enchantments.network.PluginMessageHandler.INSTANCE.isStolperLocked()) {
            // Force all movement/jump keys to not pressed
            this.settings.forwardKey.setPressed(false);
            this.settings.backKey.setPressed(false);
            this.settings.leftKey.setPressed(false);
            this.settings.rightKey.setPressed(false);
            this.settings.jumpKey.setPressed(false);
            ci.cancel();
        }
    }
}
