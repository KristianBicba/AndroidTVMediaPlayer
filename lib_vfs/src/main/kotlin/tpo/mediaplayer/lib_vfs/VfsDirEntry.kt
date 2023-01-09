package tpo.mediaplayer.lib_vfs

interface VfsDirEntry {
    val connectionString: String
    val path: String
    val name: String
    val mimeType: String?
    val isFile: Boolean
    val isDirectory: Boolean
}