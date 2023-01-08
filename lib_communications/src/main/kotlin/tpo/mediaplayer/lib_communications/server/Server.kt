package tpo.mediaplayer.lib_communications.server

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import tpo.mediaplayer.lib_communications.client.ClientMessage
import tpo.mediaplayer.lib_communications.shared.Constants
import tpo.mediaplayer.lib_communications.shared.NowPlaying
import tpo.mediaplayer.lib_communications.shared.PairingData
import java.net.ServerSocket
import java.net.Socket
import kotlin.random.Random

private sealed interface PlayerStatus {
    data class Playing(val data: NowPlaying) : PlayerStatus
    data class Error(val error: String) : PlayerStatus
    object Idle : PlayerStatus
}

class Server(private val callbacks: ServerCallbacks) : AutoCloseable {
    private val mainLock = Mutex()
    private val mainScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var server: RunningServer? = null

    private inner class RunningServer : CoroutineSocketServer<RunningServer.Client>(ServerSocket(Constants.PORT)) {
        private var pairingCode: ByteArray? = null
        private var status: PlayerStatus = PlayerStatus.Idle

        inner class Client(socket: Socket) : BaseClient(socket) {
            private var isKnown = false

            override suspend fun onLine(line: String) {
                println("onLine($line)")
                val message = try {
                    Json.decodeFromString<ClientMessage>(line)
                } catch (e: SerializationException) {
                    println("Error while decoding line: $e")
                    null
                } catch (e: IllegalArgumentException) {
                    println("Error while decoding line: $e")
                    null
                }
                if (message != null) onMessage(message)
            }

            override suspend fun onDisconnect(clientDisconnected: Boolean) {
                println("onDisconnect($clientDisconnected)")
            }

            private suspend fun onMessage(message: ClientMessage): Unit = clientLock.withLock {
                TODO()
            }
        }

        override suspend fun onOpen() {
            cbOpen()
        }

        override suspend fun onConnect(socket: Socket) = Client(socket)

        override suspend fun onClose(error: Throwable?) {
            cbClose(error)
        }

        override suspend fun closeSelf(error: Throwable?) = withContext(NonCancellable) {
            shutDown(error)
        }

        suspend fun pairingBegin(): PairingData? = serverLock.withLock {
            pairingCancel()
            val code = Random.nextBytes(6)
            val addrs = getNetworkAddresses() ?: return@withLock null
            pairingCode = code
            PairingData(code, addrs)
        }

        suspend fun pairingCancel(): Unit = serverLock.withLock {
            pairingCode = null
        }
    }

    private suspend inline fun <T> io(crossinline block: suspend () -> T) = withContext(Dispatchers.IO) { block() }

    private suspend inline fun cbOpen() = io { callbacks.onOpen(this@Server) }
    private suspend inline fun cbPairingRequest(clientName: String, clientGuid: String) =
        io { callbacks.onPairingRequest(clientName, clientGuid) }

    private suspend inline fun cbConnectionRequest(clientGuid: String) =
        io { callbacks.onConnectionRequest(clientGuid) }

    private suspend inline fun cbPlayRequest(connectionString: String) =
        io { callbacks.onPlayRequest(connectionString) }

    private suspend inline fun cbPauseRequest() = io { callbacks.onPauseRequest() }
    private suspend inline fun cbResumeRequest() = io { callbacks.onResumeRequest() }
    private suspend inline fun cbStopRequest() = io { callbacks.onStopRequest() }
    private suspend inline fun cbSeekRequest(newTimeElapsed: ULong) = io { callbacks.onSeekRequest(newTimeElapsed) }
    private suspend inline fun cbClose(error: Throwable?) = io { callbacks.onClose(error) }

    private inline fun <T> withLockBlocking(
        crossinline block: suspend () -> T
    ) = runBlocking {
        mainLock.withLock {
            block()
        }
    }

    private inline fun withLockLaunch(
        crossinline block: suspend () -> Unit
    ) {
        mainScope.launch {
            mainLock.withLock {
                block()
            }
        }
    }

    /** Starts the server if it's not already started. */
    fun open() = withLockLaunch {
        if (server != null) return@withLockLaunch
        try {
            server = RunningServer()
        } catch (e: Throwable) {
            cbClose(e)
        }
    }

    /**
     * Generates a new pairing code (returned [PairingData]) and allows devices to use it to pair. Any previous
     * pairing codes are cancelled.
     */
    fun beginPairing(): PairingData? = withLockBlocking {
        server?.pairingBegin()
    }

    /** If a pairing code is currently active, disables it. */
    fun cancelPairing(): Unit = withLockLaunch {
        server?.pairingCancel()
    }

    /** Updates the "now playing" data with the [newValue], or null to signal the playback has stopped. */
    fun updateNowPlaying(newValue: NowPlaying?): Unit = withLockLaunch {
        TODO()
    }

    /** Updates the "now playing" data with an [error] value. */
    fun updateNowPlayingError(error: String): Unit = withLockLaunch {
        TODO()
    }

    private suspend fun shutDown(error: Throwable?): Unit = mainLock.withLock {
        if (server == null) return
        server!!.closeSuspending(error)
        server = null
    }

    /** Shuts down the server if it's started. */
    override fun close() {
        mainScope.launch {
            shutDown(null)
        }
    }
}