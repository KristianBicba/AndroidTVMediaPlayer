package tpo.mediaplayer.lib_communications.server

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.sync.Mutex
import tpo.mediaplayer.lib_communications.shared.LineChannelIO
import tpo.mediaplayer.lib_communications.shared.withReentrantLock
import java.io.Closeable
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket

internal abstract class CoroutineSocketServer<ClientType : CoroutineSocketServer<ClientType>.BaseClient>(
    private val serverSocket: ServerSocket
) : Closeable {
    protected val serverLock = Mutex()
    var isClosed = false
        private set

    private val scope = CoroutineScope(Dispatchers.Default)

    private val _clients = mutableSetOf<ClientType>()
    protected val clients: Set<ClientType> = _clients

    abstract inner class BaseClient(private val socket: Socket) {
        protected val clientLock = Mutex()
        var isClosed = false
            private set

        private val incoming = Channel<String>(Channel.UNLIMITED)
        private val outgoing = Channel<String>(Channel.UNLIMITED)
        private val lineChannelIO = LineChannelIO(
            socket.getInputStream(), socket.getOutputStream(),
            incoming, outgoing
        )

        internal suspend fun runEventLoop() = coroutineScope {
            while (true) {
                val line = incoming.receiveCatching().getOrNull() ?: break
                onLine(line)
            }

            if (close(true)) { // Connection was closed by the client
                serverLock.withReentrantLock {
                    @Suppress("UNCHECKED_CAST")
                    _clients -= this@BaseClient as ClientType
                }
            }
        }

        protected suspend fun send(line: String) {
            try {
                outgoing.send(line)
            } catch (_: ClosedSendChannelException) {
            }
        }

        protected abstract suspend fun onLine(line: String)
        protected abstract suspend fun onDisconnect(clientDisconnected: Boolean) // with clientLock

        internal suspend fun close(fromEventLoop: Boolean): Boolean = clientLock.withReentrantLock {
            if (isClosed) return@withReentrantLock false
            isClosed = true

            lineChannelIO.close()
            outgoing.close()
            withContext(Dispatchers.IO) {
                try {
                    socket.close()
                } catch (_: IOException) {
                }
            }

            onDisconnect(fromEventLoop)

            true
        }
    }

    init {
        scope.launch {
            launch { jobSocketAccept() }
        }
    }

    private suspend fun jobSocketAccept() = coroutineScope {
        serverLock.withReentrantLock {
            onOpen()
        }
        while (true) {
            val socket = runInterruptible(Dispatchers.IO) {
                try {
                    serverSocket.accept()
                } catch (e: IOException) {
                    e
                }
            } ?: break

            if (socket is IOException) {
                if (!isClosed) closeSelf(socket)
                awaitCancellation()
            }

            if (socket is Throwable) throw socket
            if (socket !is Socket) continue

            serverLock.withReentrantLock {
                val client = onConnect(socket)
                _clients += client
                launch { client.runEventLoop() }
            }
        }
    }

    protected suspend fun closeClient(client: ClientType) {
        client.close(false)
        _clients -= client
    }

    protected abstract suspend fun onOpen() // with serverLock
    protected abstract suspend fun onConnect(socket: Socket): ClientType // with serverLock
    protected abstract suspend fun onClose(error: Throwable?) // with serverLock
    protected abstract suspend fun closeSelf(error: Throwable?) // NO LOCK

    suspend fun closeSuspending(error: Throwable? = null) = serverLock.withReentrantLock {
        if (isClosed) return@withReentrantLock
        isClosed = true

        onClose(error)
        withContext(Dispatchers.IO) {
            try {
                serverSocket.close()
            } catch (_: IOException) {
            }
        }
        _clients.forEach { it.close(false) }
        scope.cancel()
    }

    fun close(error: Throwable?) = runBlocking {
        closeSuspending(error)
    }

    override fun close() {
        close(null)
    }
}
