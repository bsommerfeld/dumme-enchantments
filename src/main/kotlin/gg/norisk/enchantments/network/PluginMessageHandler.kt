package gg.norisk.enchantments.network

import gg.norisk.enchantments.StupidEnchantments
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier

/**
 * PluginMessageHandler centralizes client-side handling of Plugin Message Channels (PMC)
 * using Fabric's 1.21.5 CustomPayload API.
 *
 * What it does (currently):
 * - Registers a minimal payload for the `norisk:phase` channel
 * - Registers a global receiver and logs the received bytes (and UTF-8 text, if decodable)
 */
object PluginMessageHandler {
    /**
     * Minimal payload for the `norisk:phase` plugin message channel.
     * Carries raw bytes as-is. We attempt to render them as UTF-8 for logging.
     *
     * Example server->client usage (pseudocode):
     * - Server encodes some bytes for channel `norisk:phase`
     * - Client receives them as NoriskPhasePayload and logs size plus optional text
     */
    data class NoriskPhasePayload(val data: ByteArray) : CustomPayload {
        override fun getId(): CustomPayload.Id<out CustomPayload> = ID

        companion object {
            val ID: CustomPayload.Id<NoriskPhasePayload> = CustomPayload.Id(Identifier.of("norisk", "phase"))
            val CODEC: PacketCodec<PacketByteBuf, NoriskPhasePayload> = PacketCodec.of(
                { value, buf -> buf.writeByteArray(value.data) },
                { buf -> NoriskPhasePayload(buf.readByteArray()) }
            )
        }
    }

    /**
     * Registers the `norisk:phase` payload and installs a global receiver that logs incoming messages.
     *
     * Called from StupidEnchantments.onInitializeClient(). Safe to call once during client init.
     */
    fun initClient() {
        // Register the S2C payload type
        PayloadTypeRegistry.playS2C().register(NoriskPhasePayload.ID, NoriskPhasePayload.CODEC)

        // Register a global receiver that logs incoming messages
        ClientPlayNetworking.registerGlobalReceiver(NoriskPhasePayload.ID) { payload, context ->
            // Log on the client thread to avoid heavy work on the networking thread
            context.client().execute {
                val bytes = payload.data
                val text = try {
                    bytes.toString(Charsets.UTF_8)
                } catch (_: Exception) {
                    ""
                }
                StupidEnchantments.logger.info(
                    "[PMC] Received norisk:phase ({} bytes){}",
                    bytes.size,
                    if (text.isNotEmpty()) ": \"$text\"" else ""
                )
            }
        }
    }
}
