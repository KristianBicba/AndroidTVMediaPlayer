package tpo.mediaplayer.lib_communications

import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import tpo.mediaplayer.lib_communications.client.Client
import tpo.mediaplayer.lib_communications.client.ClientCallbacks
import tpo.mediaplayer.lib_communications.client.ClientPairingHelper
import tpo.mediaplayer.lib_communications.server.Server
import tpo.mediaplayer.lib_communications.server.ServerCallbacks
import tpo.mediaplayer.lib_communications.shared.PairingData
import tpo.mediaplayer.lib_communications.shared.PlaybackStatus
import java.net.InetAddress
import kotlin.test.Test
import kotlin.test.fail

private interface ServerCallbacksPrinting : ServerCallbacks {
    override fun onOpen(server: Server) {
        println("server.onOpen($server)")
    }

    override fun onPairingRequest(clientName: String, clientGuid: String): String? {
        println("server.onPairingRequest($clientName, $clientGuid)")
        return null
    }

    override fun onConnectionRequest(clientGuid: String): String? {
        println("server.onConnectionRequest($clientGuid)")
        return null
    }

    override fun onPlayRequest(connectionString: String) {
        println("server.onPlayRequest($connectionString)")
    }

    override fun onPauseRequest() {
        println("server.onPauseRequest")
    }

    override fun onResumeRequest() {
        println("server.onResumeRequest")
    }

    override fun onStopRequest() {
        println("server.onStopRequest")
    }

    override fun onSeekRequest(newTimeElapsed: Long) {
        println("server.onSeekRequest($newTimeElapsed)")
    }

    override fun onClose(error: Throwable?) {
        println("server.onClose($error)")
    }
}

private interface ClientCallbacksPrinting : ClientCallbacks {
    override fun onUpdateNowPlaying(newValue: PlaybackStatus) {
        println("client.onUpdateNowPlaying($newValue)")
    }

    override fun onClose(error: Throwable?) {
        println("client.onClose($error)")
    }
}

class SemServer {
    val semOpen = Semaphore(1, 1)
    val semClose = Semaphore(1, 1)

    val server = Server(object : ServerCallbacksPrinting {
        override fun onOpen(server: Server) {
            super.onOpen(server)
            semOpen.release()
        }

        override fun onClose(error: Throwable?) {
            super.onClose(error)
            semClose.release()
        }
    })

    suspend fun open() {
        server.open()
        semOpen.acquire()
    }

    suspend fun close() {
        server.close()
        semClose.acquire()
    }
}

internal class FullTest {
    @Test
    fun exampleBasic() = runBlocking {
        val s = SemServer()
        s.open()
        println("Server started")

        val client = Client(object : ClientCallbacksPrinting {}, InetAddress.getLoopbackAddress())
        println("Client open")

        s.server.updateNowPlaying(PlaybackStatus.Error("fuck"))
        client.establish("hahaha")

        delay(1000)
        client.close()
        println("Client closing")

        delay(1000)
        s.close()
    }

    @Test
    fun examplePairing() = runBlocking {
        val s = SemServer()
        s.open()
        println("Server started")

        val serverPairingData = s.server.beginPairing() ?: fail("Server failed to begin pairing")

        val targetAddrs = mutableListOf<InetAddress>()
        targetAddrs += serverPairingData.addrs
        targetAddrs += InetAddress.getLoopbackAddress()!!

        val pairingData = PairingData(serverPairingData.pairingCode, targetAddrs)

        println("Now pairing")
        val pairedTarget = ClientPairingHelper.attemptToPair(pairingData, "me", "hahaha")
        if (pairedTarget != null) {
            println("Paired successfully: ${pairedTarget.name} @ ${pairedTarget.address}")
        } else {
            println("Failed")
        }

        s.close()
    }

    private fun getClient(): Client {
        val client = Client(object : ClientCallbacksPrinting {}, InetAddress.getLoopbackAddress())
        if (client.establish("test") != null) fail("Failed to establish connection")
        return client
    }

    @Test
    fun helperPlayMediaOnRealTV() {
        getClient().beginPlayback("sftp://testuser:testpassword@10.0.2.2:2022/Screen.mp4")
    }

    @Test
    fun helperObserveMedia(): Unit = runBlocking {
        val client = getClient()
        awaitCancellation()
    }

    @Test
    fun helperStopMedia() {
        getClient().stopPlayback()
    }
}