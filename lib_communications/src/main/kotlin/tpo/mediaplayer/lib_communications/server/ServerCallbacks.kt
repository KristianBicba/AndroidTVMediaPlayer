package tpo.mediaplayer.lib_communications.server

import tpo.mediaplayer.lib_communications.server.data.Client
import tpo.mediaplayer.lib_communications.server.data.ClientAuth

interface ServerCallbacks {
    /** Called when the server is started. */
    fun onOpen(server: Server)

    interface PairingRequestCallback {
        fun accept()
        fun deny(error: String)
    }

    /**
     * Called when a device has successfully connected and is requesting pairing.
     *
     * Use the [callback] to accept/deny.
     * If accepting, the [device][client] should be saved into a database, and allowed to connect when
     * [onConnectionRequest] is called with its auth data.
     */
    fun onPairingRequest(callback: PairingRequestCallback, client: Client)

    interface ConnectionRequestCallback {
        fun accept()
        fun deny(error: String)
    }

    /**
     * Called when a device is attempting to connect, and is not attempting to pair.
     *
     * Use the callback to accept/deny.
     */
    fun onConnectionRequest(callback: ConnectionRequestCallback, clientAuth: ClientAuth)

    interface FileBrowsingCallback {
        /** Attaches the [callbacks] and begins sending data to the client. */
        fun begin(callbacks: ServerFileBrowsingCallbacks)

        /** Closes the connection without an error message. */
        fun close()

        /** Closes the connection with a custom [error] message. */
        fun close(error: String)
    }

    /**
     * Called when a client wants to start browsing a remote filesystem accessed by [connectionString].
     *
     * Use the [callback] to attach a [ServerFileBrowsingCallbacks] object, which is then called when the client
     * requests data.
     */
    fun onFileBrowsingStart(callback: FileBrowsingCallback, connectionString: String)

    /** Called when the server is stopped. Also called if there was an error during startup. */
    fun onClose(error: Throwable?)
}