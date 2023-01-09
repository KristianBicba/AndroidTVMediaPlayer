package tpo.mediaplayer.lib_vfs.smb

import tpo.mediaplayer.lib_vfs.VfsDirEntry
import tpo.mediaplayer.lib_vfs.VfsFileSystem
import tpo.mediaplayer.lib_vfs.VfsFileSystemFactory
import tpo.mediaplayer.lib_vfs.VfsStat
import java.io.InputStream

class SmbFileSystem : VfsFileSystem {
    override val isClosed: Boolean
        get() = TODO("Not yet implemented")

    override val publicIdentifierString: String
        get() = TODO("Not yet implemented")

    override fun ls(path: String): List<VfsDirEntry>? {
        TODO("Not yet implemented")
    }

    override fun stat(path: String): VfsStat? {
        TODO("Not yet implemented")
    }

    override fun getInputStream(path: String): InputStream? {
        TODO("Not yet implemented")
    }

    override fun close() {
        TODO("Not yet implemented")
    }

    companion object Factory : VfsFileSystemFactory<SmbFileSystem> {
        override fun build(connectionString: String): SmbFileSystem? {
            TODO("Not yet implemented")
        }

    }
}