package gg.norisk.enchantments.mixin.camerashaker;

import gg.norisk.enchantments.utils.CameraShaker;
import me.x150.geckoAnimLib.core.EntityDuck;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.RaycastContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow
    protected abstract void moveBy(float f, float g, float h);
    
    @Shadow
    protected abstract void setPos(double x, double y, double z);
    
    @Shadow
    private Vec3d pos;
    
    @Shadow
    private BlockView area;
    
    @Shadow
    private Entity focusedEntity;

    @Inject(
            method = "update",
            at = @At(
                    // Inject before the call to clipToSpace
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/Camera;setPos(DDD)V",
                    shift = At.Shift.BY,
                    by = 1
            )
    )
    void camerashake$onUpdate(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        double x = CameraShaker.INSTANCE.getAvgX();
        double y = CameraShaker.INSTANCE.getAvgY();
        moveBy((float) .0, (float) y, (float) x);
        
        // Apply ground clipping to prevent camera from going underground
        if (((EntityDuck) focusedEntity).hasImmersiveCameraEmote() && !focusedEntity.isSpectator()) {
            float clippedY = clipToBottom((float) pos.y);
            if (clippedY != pos.y) {
                setPos(pos.x, clippedY, pos.z);
            }
        }
    }
    
    /**
     * Prevents the camera from clipping through the ground
     */
    private float clipToBottom(float yPos) {
        float minGroundDistance = 0.1F;
        float adjustedY = yPos;
        
        // Check multiple points around the camera position to ensure we don't clip through the ground
        for (int i = 0; i < 4; i++) {
            float xOffset = (i & 1) * 0.2F - 0.1F;
            float zOffset = (i >> 1 & 1) * 0.2F - 0.1F;
            
            Vec3d startPos = new Vec3d(this.pos.x + xOffset, yPos + 0.5F, this.pos.z + zOffset);
            Vec3d endPos = new Vec3d(this.pos.x + xOffset, yPos - 2.0F, this.pos.z + zOffset);
            
            HitResult hitResult = this.area
                .raycast(new RaycastContext(startPos, endPos, RaycastContext.ShapeType.VISUAL, RaycastContext.FluidHandling.NONE, this.focusedEntity));
            
            if (hitResult.getType() != HitResult.Type.MISS) {
                float groundY = (float)hitResult.getPos().y + minGroundDistance;
                if (groundY > adjustedY) {
                    adjustedY = groundY;
                }
            }
        }
        
        return adjustedY;
    }
}
