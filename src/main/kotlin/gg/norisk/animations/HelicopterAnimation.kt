package gg.norisk.animations

import gg.norisk.enchantments.impl.HelicopterEnchantmentV2
import net.minecraft.client.network.AbstractClientPlayerEntity
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.model.PlayerEntityModel
import net.minecraft.client.render.entity.state.LivingEntityRenderState
import net.minecraft.client.render.entity.state.PlayerEntityRenderState
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.Entity
import org.spongepowered.asm.mixin.injection.invoke.arg.Args

/**
 * Animation facade for Helicopter enchantment related player animations.
 * This class extracts animation responsibilities away from the enchantment logic.
 *
 * It now also exposes a small control API to start/stop the animation and adjust
 * the spin speed for a given entity. Internally this manipulates the state kept by
 * HelicopterEnchantmentV2 (helicopterMovement and flags), so callers don't need to
 * depend on the enchantment implementation details.
 *
 * Note: Currently delegates to HelicopterEnchantmentV2 to minimize invasive changes.
 * Actual animation code can be moved here later without changing callers.
 */
object HelicopterAnimation {

    fun handleRotationRendering(
        playerEntityRenderState: PlayerEntityRenderState,
        matrices: MatrixStack,
        tickDelta: Float,
        g: Float
    ) {
        HelicopterEnchantmentV2.handleRotationRendering(playerEntityRenderState, matrices, tickDelta, g)
    }

    fun PlayerEntityModel.handleApplyPlayerModelRotations(playerEntityRenderState: PlayerEntityRenderState) {
        HelicopterEnchantmentV2.run {
            this@handleApplyPlayerModelRotations.handleApplyPlayerModelRotations(playerEntityRenderState)
        }
    }

    // Java-friendly overload (no extension)
    fun applyPlayerModelRotations(model: PlayerEntityModel, playerEntityRenderState: PlayerEntityRenderState) {
        HelicopterEnchantmentV2.run {
            model.handleApplyPlayerModelRotations(playerEntityRenderState)
        }
    }

    fun handleForwardRotation(
        args: Args,
        livingEntityRenderState: LivingEntityRenderState,
        matrixStack: MatrixStack,
        vertexConsumerProvider: VertexConsumerProvider,
        i: Int
    ) {
        HelicopterEnchantmentV2.handleForwardRotation(args, livingEntityRenderState, matrixStack, vertexConsumerProvider, i)
    }

    fun handleFixBody(player: AbstractClientPlayerEntity) {
        HelicopterEnchantmentV2.handleFixBody(player)
    }

    // ===== Animation control API =====
    /** Starts the helicopter animation for the given entity. Optionally sets a spin speed (degrees per tick). */
    fun start(entity: Entity, spinSpeed: Float? = null) {
        HelicopterEnchantmentV2.run {
            val m = entity.helicopterMovement
            val updated = m.withFlying(true).copy(rotationPower = 0f, powerPresses = 0, currentRotation = m.currentRotation,
                rotationSpeed = spinSpeed ?: m.rotationSpeed)
            entity.helicopterMovement = updated
            entity.isHelicopterFlying = true
        }
    }

    /** Stops the helicopter animation and resets startup power/presses. */
    fun stop(entity: Entity) {
        HelicopterEnchantmentV2.run {
            val m = entity.helicopterMovement
            val updated = m.withFlying(false).withRotationPower(0f).withPowerPresses(0).withRotation(0f)
            entity.helicopterMovement = updated
            entity.isHelicopterFlying = false
        }
    }

    /** Returns whether the helicopter animation is currently active (flying). */
    fun isRunning(entity: Entity): Boolean {
        HelicopterEnchantmentV2.run {
            return entity.helicopterMovement.isFlying || entity.isHelicopterFlying
        }
    }

    /** Sets the spin speed (degrees per tick) without changing running state. */
    fun setSpinSpeed(entity: Entity, spinSpeed: Float) {
        HelicopterEnchantmentV2.run {
            entity.helicopterMovement = entity.helicopterMovement.copy(rotationSpeed = spinSpeed)
        }
    }

    /** Gets the current configured spin speed (degrees per tick). */
    fun getSpinSpeed(entity: Entity): Float {
        HelicopterEnchantmentV2.run {
            return entity.helicopterMovement.rotationSpeed
        }
    }
}
