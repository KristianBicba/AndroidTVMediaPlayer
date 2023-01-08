package tpo.mediaplayer.lib_communications.server

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import tpo.mediaplayer.lib_communications.shared.InstantAsTimespecSerializer
import java.time.Instant

@Serializable
sealed interface ServerMessage {
    sealed interface IPairing : ServerMessage
    sealed interface IConnect : ServerMessage
    sealed interface IEstablished : ServerMessage

    @Serializable
    @SerialName("PairingAccepted")
    data class PairingAccepted(val myName: String) : IPairing

    @Serializable
    @SerialName("ConnectionAccepted")
    object ConnectionAccepted : IConnect

    @Serializable
    @SerialName("PairingOrConnectionDenied")
    data class PairingOrConnectionDenied(val error: String) : IPairing, IConnect

    @Serializable
    @SerialName("UpdateNowPlaying")
    data class UpdateNowPlaying(val update: NowPlayingType) : IEstablished {
        @Serializable
        sealed interface NowPlayingType {
            @Serializable
            @SerialName("Playing")
            data class Playing(
                val mediaInfo: MediaInfo?,
                val timeElapsed: ULong,
                @Serializable(with = InstantAsTimespecSerializer::class)
                val timeUpdated: Instant,
                val isPaused: Boolean
            ) : NowPlayingType {
                @Serializable
                data class MediaInfo(
                    val vfsIdentifier: String,
                    val filePath: String,
                    val mediaName: String,
                    val timeTotal: ULong,
                )
            }

            @Serializable
            @SerialName("Error")
            data class Error(val error: String) : NowPlayingType

            @Serializable
            @SerialName("Idle")
            object Idle : NowPlayingType
        }
    }
}
