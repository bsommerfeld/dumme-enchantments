package gg.norisk.satisfying

import gg.norisk.datatracker.entity.getSyncedData
import gg.norisk.datatracker.entity.setSyncedData
import gg.norisk.datatracker.entity.syncedValueChangeEvent
import gg.norisk.enchantments.EnchantmentUtils.sound
import gg.norisk.enchantments.StupidEnchantments.MOD_ID
import gg.norisk.enchantments.StupidEnchantments.logger
import gg.norisk.enchantments.StupidEnchantments.toId
import gg.norisk.enchantments.sound.SoundRegistry
import gg.norisk.satisfying.SatisfyingTrail.spawnAfterImage
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.AbstractClientPlayerEntity
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.RenderLayer.MultiPhaseParameters
import net.minecraft.client.render.RenderPhase
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormat.DrawMode
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.sound.MovingSoundInstance
import net.minecraft.client.sound.SoundInstance
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageTypes
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.entity.effect.StatusEffectCategory
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Items
import net.minecraft.particle.ParticleTypes
import net.minecraft.potion.Potion
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.Identifier
import net.minecraft.util.Util
import net.minecraft.util.math.Vec3d
import net.silkmc.silk.commands.command
import net.silkmc.silk.core.entity.modifyVelocity
import net.silkmc.silk.core.event.EntityEvents
import net.silkmc.silk.core.item.itemStack
import net.silkmc.silk.core.item.setPotion
import org.ladysnake.satin.api.managed.ManagedCoreShader
import org.ladysnake.satin.api.managed.ShaderEffectManager
import java.util.function.BiFunction
import java.util.function.Function
import kotlin.time.Duration.Companion.seconds


@Suppress("INACCESSIBLE_TYPE")
object SatisfyingSuperStar {
    val ENTITY_TRANSLUCENT_CHROMA: ManagedCoreShader = ShaderEffectManager.getInstance().manageCoreShader(
        "rendertype_entity_translucent".toId(), VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL
    )
    val ENTITY_SOLID_CHROMA: ManagedCoreShader = ShaderEffectManager.getInstance().manageCoreShader(
        "rendertype_entity_solid".toId(), VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL
    )

    val ENTITY_TRANSLUCENT: BiFunction<Identifier, Boolean, RenderLayer> =
        Util.memoize { identifier: Identifier, boolean_: Boolean ->
            val multiPhaseParameters =
                MultiPhaseParameters.builder()
                    .program(RenderPhase.ShaderProgram(ENTITY_TRANSLUCENT_CHROMA::getProgram))
                    .texture(RenderPhase.Texture(identifier, false, false))
                    .transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY).cull(RenderPhase.DISABLE_CULLING)
                    .lightmap(RenderPhase.ENABLE_LIGHTMAP).overlay(RenderPhase.ENABLE_OVERLAY_COLOR).build(boolean_)
            RenderLayer.of(
                "entity_translucent",
                VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
                VertexFormat.DrawMode.QUADS,
                1536,
                true,
                true,
                multiPhaseParameters
            )
        }


    val ENTITY_SOLID: Function<Identifier, RenderLayer> = Util.memoize { identifier: Identifier ->
        val multiPhaseParameters = MultiPhaseParameters.builder()
            .program(RenderPhase.ShaderProgram(ENTITY_SOLID_CHROMA::getProgram))
            .texture(RenderPhase.Texture(identifier, false, false))
            .transparency(RenderPhase.NO_TRANSPARENCY)
            .lightmap(RenderPhase.ENABLE_LIGHTMAP)
            .overlay(RenderPhase.ENABLE_OVERLAY_COLOR)
            .build(true)
        RenderLayer.of(
            "entity_solid",
            VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
            DrawMode.QUADS,
            1536,
            true,
            false,
            multiPhaseParameters
        )
    }

    fun Entity.onTick() {
        if (this is SatisfyingTrail.AfterImagePlayer) return
        if (this !is LivingEntity) return
        if (!world.isClient) {
            if (hasStatusEffect(SUPER_STAR_EFFECT_REGISTRY)) {
                if (!isSatisfyingSuperMario) isSatisfyingSuperMario = true
            } else {
                if (isSatisfyingSuperMario) isSatisfyingSuperMario = false
            }
        }
        if (isSatisfyingSuperMario) {
            if (!world.isClient) {
                for (otherEntity in world.getOtherEntities(this, this.boundingBox.expand(1.2)) {
                    !it.isSpectator && it !is PlayerEntity && it.isAlive && it.canHit()
                }) {
                    otherEntity.sound(SoundEvents.ENTITY_PUFFER_FISH_BLOW_OUT, 1f, 1.5f)
                    otherEntity.modifyVelocity(Vec3d(0.0, 0.85, 0.0))
                    otherEntity.kill()
                }
            }

            if (this is AbstractClientPlayerEntity) {
                spawnAfterImage(
                    this,
                    MinecraftClient.getInstance().renderTickCounter.getTickDelta(false),
                    0.25.seconds
                ) {
                    it.isSatisfyingSuperMario = true
                }
            }
            repeat(7) {
                world.addParticle(
                    ParticleTypes.ELECTRIC_SPARK,
                    this.getParticleX(0.5),
                    this.randomBodyY,
                    this.getParticleZ(0.5),
                    0.001,
                    0.001,
                    0.001,
                )
            }
        }
    }

    var Entity.isSatisfyingSuperMario: Boolean
        get() = this.getSyncedData<Boolean>("$MOD_ID:isSatisfyingSuperMario") ?: false
        set(value) {
            this.setSyncedData("$MOD_ID:isSatisfyingSuperMario", value)
        }

    class SuperStarSoundInstance(private val entity: PlayerEntity) :
        MovingSoundInstance(SoundRegistry.STAR_SOUND, SoundCategory.NEUTRAL, SoundInstance.createRandom()) {
        var fadeTime = 20
        var isFading = false

        init {
            this.repeat = true
            this.repeatDelay = 0
            this.volume = 0.01f
        }

        override fun tick() {
            if (isFading) {
                --fadeTime
                this.volume *= 0.9f
                if (fadeTime < 0) {
                    this.setDone()
                    return
                }
            }

            this.x = entity.x.toFloat().toDouble()
            this.y = entity.y.toFloat().toDouble()
            this.z = entity.z.toFloat().toDouble()

            if (!entity.isRemoved && entity.isSatisfyingSuperMario) {
                this.volume = 0.75f
                this.pitch = 1f
            } else {
                isFading = true
            }
        }
    }

    val SUPER_STAR_EFFECT_REGISTRY = Registry.registerReference(
        Registries.STATUS_EFFECT,
        "super_star".toId(),
        object : StatusEffect(StatusEffectCategory.BENEFICIAL, 0xFFD700) {
        })
    val SUPER_STAR_POTION = Registry.registerReference(
        Registries.POTION, "super_star".toId(), Potion(
            StatusEffectInstance(SUPER_STAR_EFFECT_REGISTRY, (20.seconds.inWholeMilliseconds / 50).toInt(), 0)
        )
    )


    fun initServer() {
        SUPER_STAR_EFFECT_REGISTRY
        SUPER_STAR_POTION

        EntityEvents.checkInvulnerability.listen { event ->
            if (!event.source.isOf(DamageTypes.GENERIC_KILL)) {
                val player = event.entity as? PlayerEntity? ?: return@listen
                if (player.isSatisfyingSuperMario) {
                    event.isInvulnerable.set(true)
                }
            }
        }
    }

    fun initClient() {
        logger.info("Loaded Shader: $ENTITY_TRANSLUCENT_CHROMA")
        logger.info("Loaded Shader: $ENTITY_SOLID_CHROMA")

        syncedValueChangeEvent.listen { event ->
            if (event.key != "$MOD_ID:isSatisfyingSuperMario") return@listen
            val player = event.entity as? AbstractClientPlayerEntity? ?: return@listen
            if (player is SatisfyingTrail.AfterImagePlayer) return@listen
            if (player.isSatisfyingSuperMario) {
                MinecraftClient.getInstance().soundManager.play(SuperStarSoundInstance(player))
            } else {

            }
        }

        if (!FabricLoader.getInstance().isDevelopmentEnvironment) return
        command("satisfying") {
            literal("superstar") {
                runs {
                    val player = this.source.playerOrThrow
                    player.giveItemStack(itemStack(Items.POTION) {
                        this.setPotion(SUPER_STAR_POTION)
                    })
                    //player.isSatisfyingSuperMario = !player.isSatisfyingSuperMario
                }
            }
        }
    }

    fun modifyMovementSpeed(player: PlayerEntity, original: Float): Float {
        return if (player.isSatisfyingSuperMario) original * 1.5f else original
    }
}
