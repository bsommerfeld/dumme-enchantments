package gg.norisk.enchantments.utils

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minecraft.entity.EntityType
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier
import kotlin.jvm.optionals.getOrNull

object EntityTypeSerializer : KSerializer<EntityType<*>> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("EntityType", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: EntityType<*>) {
        val identifier = Registries.ENTITY_TYPE.getId(value)
        encoder.encodeString(identifier.toString())
    }

    override fun deserialize(decoder: Decoder): EntityType<*> {
        val identifierString = decoder.decodeString()
        val identifier = Identifier.of(identifierString)
        return Registries.ENTITY_TYPE.getOptionalValue(identifier).getOrNull()
            ?: throw IllegalArgumentException("Unknown EntityType: $identifierString")
    }
} 