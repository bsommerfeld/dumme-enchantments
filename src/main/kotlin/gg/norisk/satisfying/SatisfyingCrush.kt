/*package gg.norisk.satisfying

import gg.norisk.datatracker.entity.getSyncedData
import gg.norisk.datatracker.entity.setSyncedData
import gg.norisk.enchantments.EnchantmentRegistry.crush
import gg.norisk.enchantments.EnchantmentUtils.blockPos
import gg.norisk.enchantments.EnchantmentUtils.getLevel
import gg.norisk.enchantments.EnchantmentUtils.sound
import gg.norisk.enchantments.RenderUtils
import gg.norisk.enchantments.StupidEnchantments.MOD_ID
import gg.norisk.enchantments.sound.SoundRegistry
import gg.norisk.enchantments.utils.Animation
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.minecraft.block.Blocks
import net.minecraft.block.PistonBlock
import net.minecraft.block.PistonExtensionBlock
import net.minecraft.block.PistonHeadBlock
import net.minecraft.block.enums.PistonType
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.Entity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.passive.IronGolemEntity
import net.minecraft.item.ItemStack
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.Direction
import kotlin.time.Duration.Companion.seconds

object SatisfyingCrush {
    fun initClient() {
        WorldRenderEvents.AFTER_ENTITIES.register(WorldRenderEvents.AfterEntities {
            for (entity in it.world().entities) {
                val animation = entity.satisfyingCrush
                if (animation == Animation.ZERO) continue
                applyCrushRender(
                    entity,
                    it.tickCounter().getTickDelta(false),
                    it.matrixStack() ?: continue,
                    animation
                )
            }
        })
    }

    var Entity.satisfyingCrush: Animation
        get() = this.getSyncedData<Animation>("$MOD_ID:SatisfyingCrush") ?: Animation.ZERO
        set(value) {
            this.setSyncedData("$MOD_ID:SatisfyingCrush", value)
        }

    fun Entity.onTick() {
        if (world.isClient) return
        val animation = satisfyingCrush

        if (animation != Animation.ZERO && !animation.isDone) {
            val time = (animation.get() * 100).toInt()
            if (time.mod(20) == 0 || time <= 3 || animation.isDone) {
                if (this is IronGolemEntity) {
                    this.damage(damageSources.generic(), 20f)
                }
                sound(SoundEvents.ENTITY_IRON_GOLEM_DAMAGE)
            }
            //if (animation.get().toInt() * 100 % 60 == 0) {
            // }
        }
    }

    fun applyCrushRender(
        entity: Entity,
        f: Float,
        matrixStack: MatrixStack,
        animation: Animation
    ) {
        val pos = entity.getLerpedPos(f).add(-0.5, (entity.standingEyeHeight + 0.4) * animation.get(), -0.5)
        var blockState2 = Blocks.PISTON_HEAD
            .defaultState
            .with<PistonType, PistonType>(PistonExtensionBlock.TYPE, PistonType.DEFAULT)
            .with<Direction, Direction>(
                PistonHeadBlock.FACING,
                Direction.DOWN
            )
        var blockState3 = Blocks.PISTON.defaultState.with(PistonBlock.EXTENDED, true).with<Direction, Direction>(
            PistonHeadBlock.FACING,
            Direction.DOWN
        )
        matrixStack.push()
        RenderUtils.renderBlock(matrixStack, pos, blockState2, pos.blockPos)
        matrixStack.pop()
        RenderUtils.renderBlock(matrixStack, pos.add(0.0, 1.0, 0.0), blockState3, pos.blockPos.up())
    }

    fun applyTargetDamage(world: ServerWorld, entity: Entity, damageSource: DamageSource, itemStack: ItemStack?) {
        val squishLevel = crush.getLevel(itemStack) ?: return
        val duration = 7.seconds
        val animation = Animation(1f, 0.1f, duration, Animation.Easing.LINEAR)
        entity.satisfyingCrush = animation
        entity.sound(SoundRegistry.HYDRAULIC_PRESS_START)
    }

    fun <T : Entity> handleCrushRendering(livingEntity: T, matrixStack: MatrixStack) {
        val animation = livingEntity.satisfyingCrush
        if (animation != Animation.ZERO) {
            val scale = animation.get()
            matrixStack.scale(1f, scale, 1f)
        }
    }
}
*/