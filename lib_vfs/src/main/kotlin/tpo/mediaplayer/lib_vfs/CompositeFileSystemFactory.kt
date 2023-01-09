package tpo.mediaplayer.lib_vfs

import tpo.mediaplayer.lib_vfs.sftp.SftpFileSystem

class CompositeFileSystemFactory(
    private val factories: List<VfsFileSystemFactory<*>>
) : VfsFileSystemFactory<VfsFileSystem> {
    constructor(vararg factories: VfsFileSystemFactory<*>) : this(factories.asList())

    override fun build(connectionString: String): VfsFileSystem? {
        for (factory in factories) {
            return factory.build(connectionString) ?: continue
        }
        return null
    }
}

object DefaultFileSystemFactory : VfsFileSystemFactory<VfsFileSystem> by CompositeFileSystemFactory(
    SftpFileSystem
)