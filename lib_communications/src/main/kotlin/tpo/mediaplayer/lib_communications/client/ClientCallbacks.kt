package tpo.mediaplayer.lib_communications.client

import tpo.mediaplayer.lib_communications.shared.PlaybackStatus

/**
 * Functions that get called in an IO thread. Allowed to perform blocking operations, but not CPU intensive work.
 * This is not the main thread, you shouldn't update the UI from here.
 */
interface ClientCallbacks {
    /** Called when the server has sent a new "now playing" update. */
    fun onUpdateNowPlaying(newValue: PlaybackStatus)

    /**
     * Called when the socket was disconnected, or if there was an error during connection establishment.
     *
     * Once this function is called, any calls to [Client] have undefined behaviour.
     */
    fun onClose(error: Throwable?)
}