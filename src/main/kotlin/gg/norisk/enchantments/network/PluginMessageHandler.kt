package gg.norisk.enchantments.network

import gg.norisk.emote.ext.playEmote
import gg.norisk.emote.ext.stopEmote
import gg.norisk.enchantments.StupidEnchantments
import gg.norisk.enchantments.StupidEnchantments.toId
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier
import java.io.ByteArrayInputStream
import java.io.DataInputStream

/**
 * Client-side handler for the plugin messaging channel "norisk:phase".
 *
 * Responsibilities:
 * - Register payload and global receiver for the channel.
 * - Log received data (raw size and optional UTF-8 text).
 * - Trigger the "stolper" animation on messages of type=animation, payload=stolper.
 */
object PluginMessageHandler {

    @Volatile
    private var stolperLocked: Boolean = false
    fun isStolperLocked(): Boolean = stolperLocked

    /**
     * Minimal payload wrapper for the "norisk:phase" channel.
     * Provides the PacketCodec and static channel id.
     */
    data class PhasePayload(val data: ByteArray) : CustomPayload {
        override fun getId(): CustomPayload.Id<out CustomPayload> = ID

        companion object {
            val ID: CustomPayload.Id<PhasePayload> = CustomPayload.Id(Identifier.of("norisk", "phase"))
            val CODEC: PacketCodec<PacketByteBuf, PhasePayload> = PacketCodec.of(
                { value, buf -> buf.writeByteArray(value.data) },
                { buf ->
                    val readableBytes = buf.readableBytes()
                    val data = ByteArray(readableBytes)
                    buf.readBytes(data)
                    PhasePayload(data)
                }
            )
        }
    }

    private val STOLPER_EMOTE_ID = "emotes/stolpernv2.animation.json".toId()

    /** Parsed representation of the expected writeUTF(type) + writeUTF(payload) transport. */
    private data class Parsed(val type: String, val payload: String)

    /**
     * Parses writeUTF-encoded [type, payload] pairs.
     * Returns null if the data is not in the expected format.
     */
    private fun parseWriteUtf(bytes: ByteArray): Parsed? {
        return try {
            DataInputStream(ByteArrayInputStream(bytes)).use { din ->
                val type = din.readUTF()
                val payload = din.readUTF()
                Parsed(type, payload)
            }
        } catch (_: Throwable) {
            null
        }
    }

    /** Best-effort UTF-8 decode for logging and heuristics; returns empty string on failure. */
    private fun decodeUtf8(bytes: ByteArray): String = try {
        bytes.toString(Charsets.UTF_8)
    } catch (_: Exception) {
        ""
    }

    /** Log a successfully parsed writeUTF message. */
    private fun logPhaseParsed(parsed: Parsed) {
        StupidEnchantments.logger.info(
            "[PMC] norisk:phase parsed -> type='{}', payload='{}'",
            parsed.type,
            parsed.payload
        )
    }

    /** Log a received payload with size and optional decoded text. */
    private fun logPhaseReceived(bytes: ByteArray, text: String) {
        StupidEnchantments.logger.info(
            "[PMC] Received norisk:phase ({} bytes){}",
            bytes.size,
            if (text.isNotEmpty()) ": \"$text\"" else ""
        )
    }

    private fun isAnimationStolperStart(type: String, payload: String): Boolean =
        type.equals("animation:start", ignoreCase = true) && payload.equals("stolper", ignoreCase = true)

    private fun isAnimationStolperStop(type: String, payload: String): Boolean =
        type.equals("animation:stop", ignoreCase = true) && payload.equals("stolper", ignoreCase = true)

    /** Handles a parsed message; returns true if an action was taken. */
    private fun maybeHandleParsed(parsed: Parsed, context: ClientPlayNetworking.Context): Boolean {
        if (isAnimationStolperStart(parsed.type, parsed.payload)) {
            playStolper(context, "parsed writeUTF message")
            return true
        } else if (isAnimationStolperStop(parsed.type, parsed.payload)) {
            stopStolper(context, "parsed writeUTF message")
            return true
        }
        return false
    }

    /**
     * Registers the payload and installs a global receiver.
     * Safe to call once during client initialization.
     */
    fun initClient() {
        PayloadTypeRegistry.playS2C().register(PhasePayload.ID, PhasePayload.CODEC)

        ClientPlayNetworking.registerGlobalReceiver(PhasePayload.ID) { payload, context ->
            context.client().execute {
                val bytes = payload.data

                val parsed = parseWriteUtf(bytes)
                if (parsed != null) {
                    logPhaseParsed(parsed)
                    if (maybeHandleParsed(parsed, context)) {
                        return@execute
                    }
                }

                val text = decodeUtf8(bytes)
                logPhaseReceived(bytes, text)
                throw IllegalArgumentException("Could not parse plugin message")
            }
        }
    }

    /** Plays the Stolper emote for the current client player and logs the outcome. */
    private fun playStolper(context: ClientPlayNetworking.Context, source: String) {
        try {
            val player = context.client().player
            if (player != null) {
                player.playEmote(STOLPER_EMOTE_ID)
                stolperLocked = true
                StupidEnchantments.logger.info("[PMC] Triggered Stolper animation ($source) - inputs locked")
            } else {
                StupidEnchantments.logger.warn("[PMC] Could not trigger Stolper animation: client player is null")
            }
        } catch (t: Throwable) {
            StupidEnchantments.logger.error("[PMC] Failed to play Stolper animation", t)
        }
    }

    /**
     * Stops the Stolper animation for the client player, if available. Logs relevant information
     * or errors during the process.
     *
     * @param context The networking context which provides access to the client and player instance.
     * @param source A string identifier indicating the source or reason for stopping the animation.
     */
    private fun stopStolper(context: ClientPlayNetworking.Context, source: String) {
        try {
            val player = context.client().player
            if (player != null) {
                player.stopEmote(STOLPER_EMOTE_ID)
                stolperLocked = false
                StupidEnchantments.logger.info("[PMC] Stopped Stolper animation ($source) - inputs unlocked")
            } else {
                StupidEnchantments.logger.warn("[PMC] Could not stop Stolper animation: client player is null")
            }
        } catch (t: Throwable) {
            StupidEnchantments.logger.error("[PMC] Failed to stop Stolper animation", t)
        }
    }
}
