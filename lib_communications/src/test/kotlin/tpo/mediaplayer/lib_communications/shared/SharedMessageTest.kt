package tpo.mediaplayer.lib_communications.shared

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import tpo.mediaplayer.lib_communications.server.ServerMessage
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

internal class SharedMessageTest {
    private inline fun <reified Base, reified T : Base> testMessage(msgT: T) {
        val msg: Base = msgT
        println(msg)
        val enc = Json.encodeToString(msg)
        println(enc)
        val dec = Json.decodeFromString<Base>(enc)
        assertEquals(msg, dec)
    }

    @Test
    fun testServerUpdateNowPlaying() {
        testMessage<ServerMessage, _>(
            ServerMessage.UpdateNowPlaying(
                ServerMessage.UpdateNowPlaying.NowPlayingType.Playing(
                    ServerMessage.UpdateNowPlaying.NowPlayingType.Playing.MediaInfo(
                        "smb://testuser@10.0.2.2",
                        "/movies/2022/A Movie/A Movie-x264.mkv",
                        "A Movie",
                        1000 * 60 * 120
                    ),
                    1000,
                    Instant.ofEpochSecond(1673211080, 421_000_000),
                    Instant.ofEpochSecond(1673211080, 421_000_000),
                    false
                )
            )
        )
    }
}