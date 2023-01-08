package tpo.mediaplayer.lib_communications.client

import java.net.InetAddress

/**
 * The main class used for connecting to and interacting with the server. All public methods are blocking, so use
 * a thread!
 */
class Client(private val callbacks: ClientCallbacks, connectionAddress: InetAddress) : AutoCloseable {
    /** True if the client is connected to the server and allowed to send commands. */
    var isEstablished = false
        private set

    /**
     * Attempt to pair with the server.
     *
     * Returns null if successful, error message otherwise.
     * [isEstablished] will be true if successful, allowing you to send commands without reconnecting.
     */
    fun pair(clientName: String, clientGuid: String): String? {
        TODO("Not yet implemented")
    }

    /**
     * Attempt to establish a session with the server.
     *
     * Returns null if successful, error message otherwise.
     * [isEstablished] will be true if successful, allowing you to send commands.
     * Note that the only way to close an established connection is to close the socket ([Client] object),
     * and calling [establish] or [pair] on an already established [Client] will return an error.
     */
    fun establish(clientGuid: String): String? {
        TODO("Not yet implemented")
    }

    /**
     * The playback operations are idempotent (can be called multiple times) and do not cause an error if playback is
     * in the wrong state. They all require [isEstablished] = true.
     */

    /**
     * Requests playback of a file described by [connectionString].
     *
     * Returns immediately, once the server sends a response a callback will be called
     * (one of [ClientCallbacks.onUpdateNowPlaying] or [ClientCallbacks.onPlaybackStop]).
     */
    fun beginPlayback(connectionString: String) {
        TODO("Not yet implemented")
    }

    fun pausePlayback() {
        TODO("Not yet implemented")
    }

    fun resumePlayback() {
        TODO("Not yet implemented")
    }

    fun stopPlayback() {
        TODO("Not yet implemented")
    }

    fun seekPlayback(newTimeElapsed: ULong) {
        TODO("Not yet implemented")
    }

    /** Closes the connection to the server, if not already closed. */
    override fun close() {
        TODO("Not yet implemented")
    }
}

