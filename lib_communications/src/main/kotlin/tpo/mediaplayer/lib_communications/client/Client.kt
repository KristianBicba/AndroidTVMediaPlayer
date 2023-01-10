package tpo.mediaplayer.lib_communications.client

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import tpo.mediaplayer.lib_communications.server.ServerMessage
import tpo.mediaplayer.lib_communications.shared.Constants
import tpo.mediaplayer.lib_communications.shared.LineChannelIO
import tpo.mediaplayer.lib_communications.shared.NowPlaying
import tpo.mediaplayer.lib_communications.shared.PlaybackStatus
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

interface ClientPairingResult {
    data class Success(val tvName: String) : ClientPairingResult
    data class Error(val error: String) : ClientPairingResult
}

/**
 * The main class used for connecting to and interacting with the server. All public methods are blocking, so use
 * a thread!
 *
 * The connection will be established immediately, so catch any [IOException]s when calling the constructor.
 */
class Client(private val callbacks: ClientCallbacks, connectionAddress: InetAddress) : AutoCloseable {
    /** True if the client is connected to the server and allowed to send commands. */
    var isEstablished = false
        private set

    var isClosed = false
        private set

    private val lock = Mutex()
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val incoming = Channel<String>(Channel.UNLIMITED)
    private val outgoing = Channel<String>(Channel.UNLIMITED)
    private val lineChannelIO: LineChannelIO
    private val socket: Socket

    private var connectionResult: CompletableDeferred<ServerMessage.IConnect>? = null
    private var pairingResult: CompletableDeferred<ServerMessage.IPairing>? = null

    private var playbackStatus: PlaybackStatus = PlaybackStatus.Idle

    init {
        try {
            socket = Socket()
            socket.connect(InetSocketAddress(connectionAddress, Constants.PORT), 1000)
            lineChannelIO = LineChannelIO(
                socket.getInputStream(), socket.getOutputStream(),
                incoming, outgoing
            )
        } catch (e: IOException) {
            runBlocking {
                closeSuspending(e)
            }
            throw e
        }

        scope.launch { runEventLoop() }
    }

    private suspend fun runEventLoop() {
        while (true) {
            val line = incoming.receiveCatching().getOrNull() ?: break
            onLine(line)
        }

        closeSuspending()
    }

    private suspend fun onLine(line: String) {
        println("Client.onLine($line)")
        val message = try {
            Json.decodeFromString<ServerMessage>(line)
        } catch (e: SerializationException) {
            println("Error while decoding line: $e")
            null
        } catch (e: IllegalArgumentException) {
            println("Error while decoding line: $e")
            null
        }
        if (message != null) onMessage(message)
    }

    private suspend fun onMessage(message: ServerMessage): Unit = lock.withLock {
        when (message) {
            is ServerMessage.PairingAccepted -> {
                if (isEstablished || pairingResult == null) {
                    println("Unexpected message: $message")
                    return@withLock
                }
                pairingResult!!.complete(message)
                pairingResult = null
                isEstablished = true
            }
            is ServerMessage.ConnectionAccepted -> {
                if (isEstablished || connectionResult == null) {
                    println("Unexpected message: $message")
                    return@withLock
                }
                connectionResult!!.complete(message)
                connectionResult = null
                isEstablished = true
            }
            is ServerMessage.PairingOrConnectionDenied -> {
                if (isEstablished || (connectionResult == null && pairingResult == null)) {
                    println("Unexpected message: $message")
                    return@withLock
                }
                connectionResult?.complete(message)
                connectionResult = null
                pairingResult?.complete(message)
                pairingResult = null
            }
            is ServerMessage.UpdateNowPlaying -> {
                if (!isEstablished) {
                    println("Unexpected message: $message")
                    return@withLock
                }
                val newStatus: PlaybackStatus = when (val update = message.update) {
                    is ServerMessage.UpdateNowPlaying.NowPlayingType.Playing -> {
                        val mediaInfo = update.mediaInfo?.let {
                            NowPlaying.MediaInfo(
                                it.vfsIdentifier,
                                it.filePath,
                                it.mediaName,
                                it.timeTotal
                            )
                        } ?: (playbackStatus as? PlaybackStatus.Playing)?.data?.mediaInfo ?: run {
                            println("Got sparse UpdateNowPlaying.Playing, but no media information was stored before")
                            return@withLock
                        }

                        val nowPlaying = NowPlaying(
                            mediaInfo,
                            update.timeElapsed,
                            update.timeUpdated,
                            if (update.isPaused) NowPlaying.Status.PAUSED else NowPlaying.Status.PLAYING
                        )

                        PlaybackStatus.Playing(nowPlaying)
                    }
                    is ServerMessage.UpdateNowPlaying.NowPlayingType.Error -> PlaybackStatus.Error(update.error)
                    is ServerMessage.UpdateNowPlaying.NowPlayingType.Idle -> PlaybackStatus.Idle
                }
                playbackStatus = newStatus
                cbUpdateNowPlaying(newStatus)
            }
        }
    }

    private suspend fun send(message: ClientMessage) {
        val line = Json.encodeToString(message)
        try {
            outgoing.send(line)
        } catch (_: ClosedSendChannelException) {
        }
    }

    private suspend fun closeSuspending(error: Throwable? = null) = lock.withLock {
        if (isClosed) return@withLock
        isClosed = true
        isEstablished = false

        cbClose(error)

        connectionResult?.complete(ServerMessage.PairingOrConnectionDenied("Connection was closed"))
        connectionResult = null

        pairingResult?.complete(ServerMessage.PairingOrConnectionDenied("Connection was closed"))
        pairingResult = null

        outgoing.close()
        withContext(Dispatchers.IO) {
            try {
                lineChannelIO.close()
                socket.close()
            } catch (_: IOException) {
            }
        }
        scope.cancel()
    }

    private suspend inline fun <T> io(crossinline block: suspend () -> T) = withContext(Dispatchers.IO) { block() }

    private suspend inline fun cbUpdateNowPlaying(newValue: PlaybackStatus) =
        io { callbacks.onUpdateNowPlaying(newValue) }

    private suspend inline fun cbClose(error: Throwable?) = io { callbacks.onClose(error) }

    private inline fun withLockLaunch(crossinline block: suspend () -> Unit) {
        scope.launch {
            lock.withLock {
                block()
            }
        }
    }

    private inline fun withLockLaunchEstablished(crossinline block: suspend () -> Unit) {
        withLockLaunch {
            if (!isEstablished) return@withLockLaunch
            block()
        }
    }

    /**
     * Attempt to pair with the server.
     *
     * Returns null if successful, error message otherwise.
     * [isEstablished] will be true if successful, allowing you to send commands without reconnecting.
     */
    fun pair(pairingCode: ByteArray, clientName: String, clientGuid: String): ClientPairingResult = runBlocking {
        val completable = lock.withLock {
            if (isClosed) {
                return@runBlocking ClientPairingResult.Error("Connection is closed")
            }

            if (isEstablished) {
                return@runBlocking ClientPairingResult.Error("Connection is already established")
            }

            if (pairingResult != null || connectionResult != null) {
                return@runBlocking ClientPairingResult.Error("Already attempting to establish session")
            }

            val completable = CompletableDeferred<ServerMessage.IPairing>()
            pairingResult = completable

            send(ClientMessage.Pair(pairingCode, clientName, clientGuid))

            completable
        }
        when (val result = completable.await()) {
            is ServerMessage.PairingAccepted -> ClientPairingResult.Success(result.myName)
            is ServerMessage.PairingOrConnectionDenied -> ClientPairingResult.Error(result.error)
        }
    }

    /**
     * Attempt to establish a session with the server.
     *
     * Returns null if successful, error message otherwise.
     * [isEstablished] will be true if successful, allowing you to send commands.
     * Note that the only way to close an established connection is to close the socket ([Client] object),
     * and calling [establish] or [pair] on an already established [Client] will return an error.
     */
    fun establish(clientGuid: String): String? = runBlocking {
        val completable = lock.withLock {
            if (isClosed) {
                return@runBlocking "Connection is closed"
            }

            if (isEstablished) {
                return@runBlocking "Connection is already established"
            }

            if (pairingResult != null || connectionResult != null) {
                return@runBlocking "Already attempting to establish session"
            }

            val completable = CompletableDeferred<ServerMessage.IConnect>()
            connectionResult = completable

            send(ClientMessage.Connect(clientGuid))

            completable
        }
        when (val result = completable.await()) {
            is ServerMessage.ConnectionAccepted -> null
            is ServerMessage.PairingOrConnectionDenied -> result.error
        }
    }

    /**
     * The playback operations are idempotent (can be called multiple times) and do not cause an error if playback is
     * in the wrong state. They all require [isEstablished] = true.
     */

    /**
     * Requests playback of a file described by [connectionString].
     *
     * Returns immediately, once the server sends a response a callback will be called
     * ([ClientCallbacks.onUpdateNowPlaying]).
     */
    fun beginPlayback(connectionString: String) = withLockLaunchEstablished {
        send(ClientMessage.BeginPlayback(connectionString))
    }

    fun pausePlayback() = withLockLaunchEstablished { send(ClientMessage.PausePlayback) }
    fun resumePlayback() = withLockLaunchEstablished { send(ClientMessage.ResumePlayback) }
    fun stopPlayback() = withLockLaunchEstablished { send(ClientMessage.StopPlayback) }

    fun seekPlayback(newTimeElapsed: Long) = withLockLaunchEstablished {
        send(ClientMessage.SeekPlayback(newTimeElapsed))
    }

    /** Closes the connection to the server, if not already closed. */
    override fun close() = runBlocking {
        closeSuspending()
    }
}

