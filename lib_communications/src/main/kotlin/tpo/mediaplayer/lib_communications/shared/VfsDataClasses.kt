package tpo.mediaplayer.lib_communications.shared

sealed class VfsPath {
    abstract val path: String
}

data class VfsPathFile(override val path: String) : VfsPath()
data class VfsPathDir(override val path: String) : VfsPath()

data class VfsDirEntry(val displayName: String, val path: VfsPath, val mimeType: String?)

sealed class VfsPathDetails {
    abstract val path: VfsPath
}

data class VfsPathDetailsFile(
    override val path: VfsPathFile,
    val sizeBytes: ULong,
    val mimeType: String
) : VfsPathDetails()

data class VfsPathDetailsDir(
    override val path: VfsPathDir,
    val numEntries: ULong,
) : VfsPathDetails()