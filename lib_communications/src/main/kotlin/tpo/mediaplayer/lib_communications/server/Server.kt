package tpo.mediaplayer.lib_communications.server

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import tpo.mediaplayer.lib_communications.client.ClientMessage
import tpo.mediaplayer.lib_communications.shared.*
import java.net.ServerSocket
import java.net.Socket
import kotlin.random.Random

class Server(private val callbacks: ServerCallbacks) : AutoCloseable {
    private val mainLock = Mutex()
    private val mainScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var server: RunningServer? = null

    private inner class RunningServer : CoroutineSocketServer<RunningServer.Client>(ServerSocket(Constants.PORT)) {
        private var pairingCode: ByteArray? = null
        private var status: PlaybackStatus = PlaybackStatus.Idle

        inner class Client(socket: Socket) : BaseClient(socket) {
            private var clientStatusView: PlaybackStatus = PlaybackStatus.Idle
            private var isEstablished = false

            override suspend fun onLine(line: String) {
                println("Server.onLine($line)")
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

            private suspend fun onMessage(message: ClientMessage): Unit = clientLock.withReentrantLock {
                when (message) {
                    is ClientMessage.Pair -> {
                        if (isEstablished) {
                            println("Unexpected message: $message")
                            return@withReentrantLock
                        }
                        val expectedCode = serverLock.withReentrantLock { pairingCode }
                        if (expectedCode == null) {
                            send(ServerMessage.PairingOrConnectionDenied("Server is closed for pairing"))
                        } else if (!expectedCode.contentEquals(message.pairingCode)) {
                            send(ServerMessage.PairingOrConnectionDenied("Incorrect pairing code"))
                        } else {
                            val error = cbPairingRequest(message.myName, message.myGuid)
                            if (error != null) {
                                send(ServerMessage.PairingOrConnectionDenied(error))
                            } else {
                                isEstablished = true
                                send(ServerMessage.PairingAccepted("Android TV"))
                            }
                        }
                    }
                    is ClientMessage.Connect -> {
                        if (isEstablished) {
                            println("Unexpected message: $message")
                            return@withReentrantLock
                        }
                        val error = cbConnectionRequest(message.myGuid)
                        if (error != null) {
                            send(ServerMessage.PairingOrConnectionDenied(error))
                        } else {
                            isEstablished = true
                            send(ServerMessage.ConnectionAccepted)
                            serverLock.withReentrantLock {
                                sendStatus(status)
                            }
                        }
                    }
                    is ClientMessage.Heartbeat -> {
                        if (!isEstablished) {
                            println("Unexpected message: $message")
                            return@withReentrantLock
                        }
                        serverLock.withReentrantLock {
                            sendStatus(status)
                        }
                    }
                    is ClientMessage.BeginPlayback -> {
                        if (!isEstablished) {
                            println("Unexpected message: $message")
                            return@withReentrantLock
                        }
                        cbPlayRequest(message.connectionString)
                    }
                    is ClientMessage.PausePlayback -> {
                        if (!isEstablished) {
                            println("Unexpected message: $message")
                            return@withReentrantLock
                        }
                        cbPauseRequest()
                    }
                    is ClientMessage.ResumePlayback -> {
                        if (!isEstablished) {
                            println("Unexpected message: $message")
                            return@withReentrantLock
                        }
                        cbResumeRequest()
                    }
                    is ClientMessage.StopPlayback -> {
                        if (!isEstablished) {
                            println("Unexpected message: $message")
                            return@withReentrantLock
                        }
                        cbStopRequest()
                    }
                    is ClientMessage.SeekPlayback -> {
                        if (!isEstablished) {
                            println("Unexpected message: $message")
                            return@withReentrantLock
                        }
                        cbSeekRequest(message.newTimeElapsed)
                    }
                }
            }

            private suspend fun send(message: ServerMessage) = send(Json.encodeToString(message))

            private suspend fun sendStatus(newStatus: PlaybackStatus) {
                val currentStatusView = clientStatusView
                val update = if (currentStatusView is PlaybackStatus.Playing &&
                    newStatus is PlaybackStatus.Playing &&
                    currentStatusView.data.mediaInfo == newStatus.data.mediaInfo
                ) {
                    ServerMessage.UpdateNowPlaying.NowPlayingType.Playing(
                        null,
                        newStatus.data.timeElapsed,
                        newStatus.data.timeUpdated,
                        newStatus.data.status != NowPlaying.Status.PLAYING
                    )
                } else {
                    when (newStatus) {
                        is PlaybackStatus.Playing -> ServerMessage.UpdateNowPlaying.NowPlayingType.Playing(
                            ServerMessage.UpdateNowPlaying.NowPlayingType.Playing.MediaInfo(
                                newStatus.data.mediaInfo.vfsIdentifier,
                                newStatus.data.mediaInfo.filePath,
                                newStatus.data.mediaInfo.mediaName,
                                newStatus.data.mediaInfo.timeTotal
                            ),
                            newStatus.data.timeElapsed,
                            newStatus.data.timeUpdated,
                            newStatus.data.status != NowPlaying.Status.PLAYING
                        )
                        is PlaybackStatus.Error -> ServerMessage.UpdateNowPlaying.NowPlayingType.Error(newStatus.error)
                        is PlaybackStatus.Idle -> ServerMessage.UpdateNowPlaying.NowPlayingType.Idle
                    }
                }
                send(ServerMessage.UpdateNowPlaying(update))
                clientStatusView = newStatus
            }

            suspend fun updateStatus() = clientLock.withReentrantLock {
                if (!isEstablished) return@withReentrantLock
                sendStatus(status)
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

        suspend fun pairingBegin(): PairingData? = serverLock.withReentrantLock {
            pairingCancel()
            val code = Random.nextBytes(6)
            val addrs = getNetworkAddresses() ?: return@withReentrantLock null
            pairingCode = code
            PairingData(code, addrs)
        }

        suspend fun pairingCancel(): Unit = serverLock.withReentrantLock {
            pairingCode = null
        }

        suspend fun updateNowPlaying(newStatus: PlaybackStatus): Unit = serverLock.withReentrantLock {
            if (isClosed) return@withReentrantLock
            status = newStatus
            for (client in clients) {
                client.updateStatus()
            }
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
    private suspend inline fun cbSeekRequest(newTimeElapsed: Long) = io { callbacks.onSeekRequest(newTimeElapsed) }
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

    /** Updates the "now playing" data with the [newValue]. */
    fun updateNowPlaying(newValue: PlaybackStatus): Unit = withLockLaunch {
        server?.updateNowPlaying(newValue)
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