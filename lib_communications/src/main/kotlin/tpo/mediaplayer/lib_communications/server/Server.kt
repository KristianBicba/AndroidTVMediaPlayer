package tpo.mediaplayer.lib_communications.server

import tpo.mediaplayer.lib_communications.shared.Constants
import tpo.mediaplayer.lib_communications.shared.NowPlaying
import java.io.Closeable
import java.net.ServerSocket

class Server(private val callbacks: ServerCallbacks) : Closeable {
    private inner class RunningServer : Closeable {
        private val serverSocket = ServerSocket(Constants.PORT)

        override fun close() {
            TODO("Not yet implemented")
        }
    }

    private var server: RunningServer? = null

    val isListening
        get() = server != null

    /** Starts the server if it's not already started. */
    fun open() {
        if (server != null) return
        try {
            server = RunningServer()
        } catch (e: Throwable) {
            callbacks.onClose(e)
        }
    }

    /**
     * Generates a new pairing code (returned [ByteArray]) and allows devices to use it to pair. Any previous
     * pairing codes are cancelled.
     */
    fun beginPairing(): ByteArray = TODO()

    /** If a pairing code is currently active, disables it. */
    fun cancelPairing(): Unit = TODO()

    /** Updates the "now playing" data with the [newValue], or null to signal the playback has stopped. */
    fun updateNowPlaying(newValue: NowPlaying?): Unit = TODO()

    /** Shuts down the server if it's started. */
    override fun close() {
        server?.close()
        server = null
        callbacks.onClose(null)
    }
}