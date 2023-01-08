package tpo.mediaplayer.lib_communications.client

import tpo.mediaplayer.lib_communications.shared.NowPlaying

/**
 * Functions that get called in an IO thread. Allowed to perform blocking operations, but not CPU intensive work.
 * This is not the main thread, you shouldn't update the UI from here.
 */
interface ClientCallbacks {
    /** Called when the server has sent a new "now playing" update. */
    fun onUpdateNowPlaying(newValue: NowPlaying)

    /**
     * Called when the server has sent a "now playing" update indicating that playback has stopped, or an error has
     * occurred ([error] is not null).
     */
    fun onPlaybackStop(error: String?)

    /** Called when the socket was disconnected, or if there was an error during connection establishment. */
    fun onClose(error: Throwable?)
}