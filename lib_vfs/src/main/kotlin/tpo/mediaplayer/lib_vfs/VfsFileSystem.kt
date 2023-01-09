package tpo.mediaplayer.lib_vfs

import java.io.InputStream

interface VfsFileSystem : AutoCloseable {
    val isClosed: Boolean

    val publicIdentifierString: String

    fun ls(path: String): List<VfsDirEntry>?
    fun stat(path: String): VfsStat?
    fun getInputStream(path: String): InputStream?
}