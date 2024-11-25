package gg.norisk.satisfying

import com.mojang.authlib.GameProfile
import gg.norisk.enchantments.StupidEnchantments.toId
import gg.norisk.satisfying.entity.AfterImagePlayer
import gg.norisk.utils.DevUtils.uniqueId
import kotlinx.coroutines.Job
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.AbstractClientPlayerEntity
import net.minecraft.entity.Entity
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.entity.effect.StatusEffectCategory
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.potion.Potion
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.silkmc.silk.core.kotlin.ticks
import net.silkmc.silk.core.task.mcCoroutineTask
import org.spongepowered.asm.mixin.injection.invoke.arg.Args
import java.awt.Color
import java.util.*
import java.util.function.Consumer
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

object SatisfyingTrail {
    fun initClient() {
        if (!FabricLoader.getInstance().isDevelopmentEnvironment) return
    }

    fun initServer() {
        AFTER_IMAGE_EFFECT_REGISTRY
        AFTER_IMAGE_POTION
    }

    val AFTER_IMAGE_EFFECT_REGISTRY = Registry.registerReference(
        Registries.STATUS_EFFECT,
        "after_image".toId(),
        object : StatusEffect(StatusEffectCategory.BENEFICIAL, 0xCBCBCB) {
        })
    val AFTER_IMAGE_POTION = Registry.registerReference(
        Registries.POTION, "after_image".toId(), Potion(
            StatusEffectInstance(
                AFTER_IMAGE_EFFECT_REGISTRY,
                (20.seconds.inWholeMilliseconds / 50).toInt(),
                0,
                false,
                false,
                false
            )
        )
    )

    @Environment(EnvType.CLIENT)
    fun spawnAfterImage(
        base: AbstractClientPlayerEntity,
        tickDelta: Float,
        fadeDuration: Duration,
        consumer: Consumer<AfterImagePlayer>
    ) {
        val copy = AfterImagePlayer(
            base.clientWorld, GameProfile(UUID.randomUUID(), base.gameProfile.name),
            base.age + tickDelta,
            base.limbAnimator.speed,
            base.limbAnimator.pos,
            base.headYaw,
            base.bodyYaw,
            base.pitch,
            base.uniqueId
        )
        copy.fakeSkinTextures = base.skinTextures
        copy.setPosition(base.pos)
        copy.yaw = base.yaw
        copy.headYaw = base.headYaw
        copy.bodyYaw = base.bodyYaw
        copy.pitch = base.pitch
        copy.handSwingProgress = base.getHandSwingProgress(tickDelta)
        copy.resetFadeTime(fadeDuration)
        consumer.accept(copy)
        mcCoroutineTask(sync = true, client = true, delay = 1.ticks) {
            base.clientWorld.addEntity(copy)
        }
    }

    @Environment(EnvType.CLIENT)
    fun spawnAfterImages(
        base: AbstractClientPlayerEntity,
        howOften: Long,
        period: Duration,
        delay: Duration,
        fadeDuration: Duration,
        consumer: Consumer<AfterImagePlayer> = Consumer {}
    ): Job {
        return mcCoroutineTask(sync = true, client = true, howOften = howOften, period = period, delay = delay) {
            val tickDelta = MinecraftClient.getInstance().renderTickCounter.getTickDelta(false)
            spawnAfterImage(base, tickDelta, fadeDuration, consumer)
        }
    }

    fun onTick(entity: Entity) {
        if (entity.world.isClient && entity is AfterImagePlayer) {
            entity.noClip = true
        } else {
            if (entity.world.isClient && entity is AbstractClientPlayerEntity && entity.hasStatusEffect(
                    AFTER_IMAGE_EFFECT_REGISTRY
                )
            ) {
                val effect = entity.statusEffects.find { it.equals(AFTER_IMAGE_EFFECT_REGISTRY) } ?: return
                when (effect.amplifier) {
                    1 -> {
                        if (entity.age.mod(5) == 0) {
                            spawnAfterImage(
                                entity,
                                MinecraftClient.getInstance().renderTickCounter.getTickDelta(false),
                                2.seconds
                            ) {

                            }
                        }
                    }

                    2 -> {
                        if (entity.age.mod(3) == 0) {
                            spawnAfterImage(
                                entity,
                                MinecraftClient.getInstance().renderTickCounter.getTickDelta(false),
                                2.seconds
                            ) {
                                it.canFade = false
                            }
                        }
                    }

                    else -> {
                        if (entity.age.mod(10) == 0) {
                            spawnAfterImage(
                                entity,
                                MinecraftClient.getInstance().renderTickCounter.getTickDelta(false),
                                1.seconds
                            ) {

                            }
                        }
                    }
                }
            }
        }
    }

    @Environment(EnvType.CLIENT)
    fun <T> modifyAlpha(
        args: Args,
        livingEntity: T,
        f: Float,
        g: Float
    ) {
        val player = livingEntity as? AfterImagePlayer? ?: return
        args.set(4, Color(255, 255, 255, player.getFadeValue()).rgb)
    }
}
