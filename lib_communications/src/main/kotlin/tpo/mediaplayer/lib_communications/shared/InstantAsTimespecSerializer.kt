package tpo.mediaplayer.lib_communications.shared

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.Instant

@Serializable
@SerialName("Instant")
private data class InstantSurrogate(val s: Long, val n: Int)

object InstantAsTimespecSerializer : KSerializer<Instant> {
    override val descriptor = InstantSurrogate.serializer().descriptor
    override fun serialize(encoder: Encoder, value: Instant) {
        val surrogate = InstantSurrogate(value.epochSecond, value.nano)
        encoder.encodeSerializableValue(InstantSurrogate.serializer(), surrogate)
    }
    override fun deserialize(decoder: Decoder): Instant {
        val surrogate = decoder.decodeSerializableValue(InstantSurrogate.serializer())
        return Instant.ofEpochSecond(surrogate.s, surrogate.n.toLong())
    }
}