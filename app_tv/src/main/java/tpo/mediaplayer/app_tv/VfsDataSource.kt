package tpo.mediaplayer.app_tv

import android.net.Uri
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.upstream.BaseDataSource
import com.google.android.exoplayer2.upstream.DataSourceException
import com.google.android.exoplayer2.upstream.DataSpec
import tpo.mediaplayer.lib_vfs.ConnectionString
import tpo.mediaplayer.lib_vfs.DefaultFileSystemFactory
import tpo.mediaplayer.lib_vfs.VfsFileSystem
import tpo.mediaplayer.lib_vfs.VfsStat
import java.io.InputStream

class VfsDataSource(private val container: VfsContainer?) : BaseDataSource(true) {
    class VfsContainer(
        var stat: VfsStat?,
        var vfsUri: String?
    )

    private var uri: Uri? = null
    private var vfs: VfsFileSystem? = null
    private var inputStream: InputStream? = null

    private fun closeAndThrow(): Nothing {
        close()
        throw DataSourceException(PlaybackException.ERROR_CODE_IO_UNSPECIFIED)
    }

    override fun open(dataSpec: DataSpec): Long {
        container?.apply {
            stat = null
            vfsUri = null
        }
        val connectionString = ConnectionString.fromURI(dataSpec.uri.toString()) ?: closeAndThrow()
        val vfs = DefaultFileSystemFactory.build(connectionString.server.toString()) ?: closeAndThrow()
        val stat = vfs.stat(connectionString.path) ?: closeAndThrow()
        val size = stat.size ?: closeAndThrow()
        val inputStream = vfs.getInputStream(connectionString.path) ?: closeAndThrow()

        if (dataSpec.position > 0)
            inputStream.skip(dataSpec.position)

        this.container?.apply {
            this.stat = stat
            vfsUri = vfs.publicIdentifierString
        }
        this.uri = dataSpec.uri
        this.vfs = vfs
        this.inputStream = inputStream

        return size
    }

    override fun getUri() = uri

    override fun read(buffer: ByteArray, offset: Int, length: Int) =
        (inputStream ?: closeAndThrow()).read(buffer, offset, length)

    override fun close() {
        container?.apply {
            stat = null
            vfsUri = null
        }
        uri = null
        vfs?.close()
        inputStream?.close()
    }
}