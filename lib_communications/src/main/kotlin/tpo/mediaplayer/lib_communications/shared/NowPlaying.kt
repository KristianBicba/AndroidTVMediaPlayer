package tpo.mediaplayer.lib_communications.shared

import java.time.Instant

data class NowPlaying(
    val mediaInfo: MediaInfo,
    /** Number of elapsed milliseconds since the start of the file */
    val timeElapsed: Long,
    /** The timestamp when this data was last updated */
    val timeUpdated: Instant,
    val status: Status
) {
    data class MediaInfo(
        /** Unique identifier for the remote filesystem. Should not contain any secret information, and exists to allow
         * clients to determine whether or not their playback request was accepted. For example, a random number passed
         * in the connection string, and known to both server and client. */
        val vfsIdentifier: String,
        /** The full path of the media file. */
        val filePath: String,
        /** The name of the media file, either embedded or filename. */
        val mediaName: String,
        /** The length of the media file, in milliseconds */
        val timeTotal: Long,
    )

    enum class Status {
        PAUSED, PLAYING
    }
}
