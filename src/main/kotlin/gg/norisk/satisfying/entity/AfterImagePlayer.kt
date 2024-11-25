package gg.norisk.satisfying.entity

import com.mojang.authlib.GameProfile
import gg.norisk.emote.ext.playEmote
import gg.norisk.enchantments.StupidEnchantments.toId
import gg.norisk.enchantments.sound.SoundRegistry
import kotlinx.coroutines.cancel
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.OtherClientPlayerEntity
import net.minecraft.client.network.PlayerListEntry
import net.minecraft.client.util.SkinTextures
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerModelPart
import net.minecraft.sound.SoundCategory
import net.silkmc.silk.core.task.mcCoroutineTask
import java.util.*
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

open class AfterImagePlayer(
    clientWorld: ClientWorld,
    gameProfile: GameProfile,
    val copiedAnimationProgress: Float,
    val copiedLimbSpeed: Float,
    val copiedLimbPos: Float,
    val copiedHeadYaw: Float,
    val copiedBodyYaw: Float,
    val copiedPitch: Float,
    var owner: UUID,
    var canFade: Boolean = true,
) : OtherClientPlayerEntity(
    clientWorld,
    gameProfile,
) {
    var fakeSkinTextures: SkinTextures? = null
    var fadeStart = System.currentTimeMillis()
    var fadeEnd = System.currentTimeMillis()
    var isFallen = false

    override fun isInvisibleTo(playerEntity: PlayerEntity?): Boolean {
        return true
    }

    override fun tick() {
        super.tick()
        if (!canFade && age > 1000) {
            discard()
        }
        if (getFadeValue() <= 0) {
            discard()
        }
    }

    override fun canHit(): Boolean {
        return !canFade
    }

    fun getFadeValue(): Int {
        if (!canFade) return 255

        val currentTime = System.currentTimeMillis()
        val fadeDuration = fadeEnd - fadeStart

        if (fadeDuration <= 0) {
            return 0 // Wenn die Fade-Zeit null oder negativ ist, geben wir direkt 0 zurück
        }

        val timeLeft = fadeEnd - currentTime
        val fadeProgress = timeLeft.toFloat() / fadeDuration.toFloat()

        return (fadeProgress * 255).toInt().coerceIn(0, 255)
    }

    fun resetFadeTime(duration: Duration) {
        fadeStart = System.currentTimeMillis()
        this.fadeEnd = fadeStart + duration.inWholeMilliseconds
    }

    override fun getSkinTextures(): SkinTextures {
        return fakeSkinTextures ?: super.getSkinTextures()
    }

    override fun isPartVisible(modelPart: PlayerModelPart): Boolean {
        if (modelPart == PlayerModelPart.CAPE) return false
        val owner = world.getPlayerByUuid(owner)
        if (owner != null) {
            return owner.isPartVisible(modelPart)
        }
        return true
    }

    fun getNextDominoPieces(): List<AfterImagePlayer> {
        return clientWorld.getOtherEntities(this, this.boundingBox.expand(1.3)) {
            it is AfterImagePlayer && !it.canFade
        }.filterIsInstance<AfterImagePlayer>().filter { !it.isFallen }
    }

    fun fallNextPiece() {
        val nextPieces = getNextDominoPieces().sortedBy { it.distanceTo(this) }
        //println("NextPieces: ${nextPieces.size}")
        for ((index, nextDominoPiece) in nextPieces.withIndex()) {
            //println("Piece: $nextDominoPiece")
            nextDominoPiece.dominoFall(index == 0)
            return
        }
    }

    fun dominoFall(triggerNext: Boolean = true) {
        isFallen = true
        playEmote("emotes/domino.animation.json".toId())
        mcCoroutineTask(sync = true, client = true, delay = 0.06.seconds) {
            if (clientWorld == null) {
                cancel()
                return@mcCoroutineTask
            }
            clientWorld.playSoundFromEntity(
                MinecraftClient.getInstance().player,
                this@AfterImagePlayer,
                SoundRegistry.DOMINO_FALL,
                SoundCategory.PLAYERS,
                1f,
                Random.nextDouble(0.9, 1.2).toFloat()
            )
        }
        if (triggerNext) {
            mcCoroutineTask(sync = true, client = true, delay = 0.08.seconds) {
                if (clientWorld == null) {
                    cancel()
                    return@mcCoroutineTask
                }
                fallNextPiece()
            }
        }
    }

    override fun handleAttack(entity: Entity): Boolean {
        if (!canFade) {
            dominoFall()
        }
        return super.handleAttack(entity)
    }

    override fun getPlayerListEntry(): PlayerListEntry? {
        return null
    }
}
