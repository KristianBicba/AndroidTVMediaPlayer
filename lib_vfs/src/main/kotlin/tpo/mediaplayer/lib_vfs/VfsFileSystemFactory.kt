package tpo.mediaplayer.lib_vfs

interface VfsFileSystemFactory<out T : VfsFileSystem> {
    fun build(connectionString: String): T?
}