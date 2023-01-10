package tpo.mediaplayer.lib_communications.client

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface ClientMessage {
    sealed interface IPairing : ClientMessage
    sealed interface IConnect : ClientMessage
    sealed interface IEstablished : ClientMessage

    @Serializable
    @SerialName("Pair")
    data class Pair(
        val pairingCode: ByteArray,
        val myName: String,
        val myGuid: String
    ) : IPairing {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Pair

            if (!pairingCode.contentEquals(other.pairingCode)) return false
            if (myName != other.myName) return false
            if (myGuid != other.myGuid) return false

            return true
        }

        override fun hashCode(): Int {
            var result = pairingCode.contentHashCode()
            result = 31 * result + myName.hashCode()
            result = 31 * result + myGuid.hashCode()
            return result
        }
    }

    @Serializable
    @SerialName("Connect")
    data class Connect(val myGuid: String) : IConnect

    @Serializable
    @SerialName("Heartbeat")
    object Heartbeat : IEstablished

    @Serializable
    @SerialName("BeginPlayback")
    data class BeginPlayback(val connectionString: String) : IEstablished

    @Serializable
    @SerialName("PausePlayback")
    object PausePlayback : IEstablished

    @Serializable
    @SerialName("ResumePlayback")
    object ResumePlayback : IEstablished

    @Serializable
    @SerialName("StopPlayback")
    object StopPlayback : IEstablished

    @Serializable
    @SerialName("SeekPlayback")
    data class SeekPlayback(val newTimeElapsed: Long) : IEstablished
}