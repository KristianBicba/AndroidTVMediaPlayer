package tpo.mediaplayer.lib_communications.server

import tpo.mediaplayer.lib_communications.shared.*

interface ServerFileBrowsingCallbacks {
    interface DirectoryListingCallback {
        fun send(entries: List<VfsDirEntry>)
        fun error(message: String?)
    }

    /** Requests a listing of the directory [dir]. Reply by calling the methods on the [callback] interface. */
    fun requestDirectoryListing(callback: DirectoryListingCallback, dir: VfsPathDir)

    interface PathDetailsCallback {
        fun send(details: VfsPathDetails)
        fun error(message: String?)
    }

    /** Requests details about a [file]. Reply by calling the methods on the [callback] interface. */
    fun requestPathDetails(callback: PathDetailsCallback, path: VfsPath)

    interface PlayFileCallback {
        fun deny(message: String?)
    }

    /**
     * Requests playback of a [file]. Reply by calling the methods on the [callback] interface.
     *
     * The callback does not contain an accept() method. Instead, the server should update its 'now playing' status.
     */
    fun requestPlayFile(callback: PlayFileCallback, file: VfsPathFile)

    /**
     * Called when the client closes the browsing session. This is the last callback, however the underlying
     * filesystem connection must remain open if a file is currently being played from it.
     */
    fun onClose()
}