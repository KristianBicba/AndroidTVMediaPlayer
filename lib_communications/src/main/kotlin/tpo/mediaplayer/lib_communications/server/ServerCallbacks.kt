package tpo.mediaplayer.lib_communications.server

/**
 * Functions that get called in an IO thread by the server. Allowed to perform blocking operations, but not CPU
 * intensive work. This is not the main thread, you shouldn't update the UI from here.
 */
interface ServerCallbacks {
    /** Called when the server is started. */
    fun onOpen(server: Server)

    /**
     * Called when a device has successfully connected and is requesting pairing.
     *
     * Return null to accept, or return an error message to deny.
     */
    fun onPairingRequest(clientName: String, clientGuid: String): String?

    /**
     * Called when a device is attempting to connect, and is not attempting to pair.
     *
     * Return null to accept, or return an error message to deny.
     */
    fun onConnectionRequest(clientGuid: String): String?

    /**
     * Called when a client wants to play a file with the URI [connectionString].
     *
     * There is no callback, just update the "now playing" with an error or the new value. The client should detect
     * it and display the remote controls or the error, if there was one.
     */
    fun onPlayRequest(connectionString: String)

    // Same as onPlayRequest, just update "now playing".

    fun onPauseRequest()
    fun onResumeRequest()
    fun onStopRequest()
    fun onSeekRequest(newTimeElapsed: Long) // Milliseconds

    /** Called when the server is stopped. Also called if there was an error during startup. */
    fun onClose(error: Throwable?)
}