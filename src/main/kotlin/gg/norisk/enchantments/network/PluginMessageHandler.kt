package gg.norisk.enchantments.network

import gg.norisk.emote.ext.playEmote
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

    /** True if the parsed fields indicate the Stolper animation. */
    private fun isAnimationStolper(type: String, payload: String): Boolean =
        type.equals("animation", ignoreCase = true) && payload.equals("stolper", ignoreCase = true)

    /** Heuristic detection of "animation: stolper" in free-form text payloads. */
    private fun isAnimationStolperText(text: String): Boolean {
        val normalized = text.lowercase().trim()
        return when {
            normalized.contains("\"type\"") && normalized.contains("animation") &&
                    normalized.contains("\"payload\"") && normalized.contains("stolper") -> true

            normalized.contains("type=animation") && normalized.contains("payload=stolper") -> true
            normalized.startsWith("animation") && normalized.contains("stolper") -> true
            else -> false
        }
    }

    /** Handles a parsed message; returns true if an action was taken. */
    private fun maybeHandleParsed(parsed: Parsed, context: ClientPlayNetworking.Context): Boolean {
        if (isAnimationStolper(parsed.type, parsed.payload)) {
            playStolper(context, "parsed writeUTF message")
            return true
        }
        return false
    }

    /** Handles a textual fallback message; returns true if an action was taken. */
    private fun maybeHandleHeuristic(text: String, context: ClientPlayNetworking.Context): Boolean {
        if (isAnimationStolperText(text)) {
            playStolper(context, "heuristic plugin message")
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
                maybeHandleHeuristic(text, context)
            }
        }
    }

    /** Plays the Stolper emote for the current client player and logs the outcome. */
    private fun playStolper(context: ClientPlayNetworking.Context, source: String) {
        try {
            val player = context.client().player
            if (player != null) {
                player.playEmote(STOLPER_EMOTE_ID)
                StupidEnchantments.logger.info("[PMC] Triggered Stolper animation ($source)")
            } else {
                StupidEnchantments.logger.warn("[PMC] Could not trigger Stolper animation: client player is null")
            }
        } catch (t: Throwable) {
            StupidEnchantments.logger.error("[PMC] Failed to play Stolper animation", t)
        }
    }

}
