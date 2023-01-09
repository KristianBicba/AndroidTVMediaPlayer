package tpo.mediaplayer.lib_vfs.sftp

import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.sftp.FileMode
import net.schmizz.sshj.sftp.OpenMode
import net.schmizz.sshj.sftp.SFTPClient
import net.schmizz.sshj.sftp.SFTPException
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import tpo.mediaplayer.lib_vfs.*
import java.io.IOException
import java.io.InputStream

private val pathToNameRegex = Regex("""^/(?:.+/)?([^/]+)(/*)$""")

class SftpFileSystem(
    argUsername: String,
    argPassword: String,
    argHostname: String,
    argPort: Int = 22
) : VfsFileSystem {
    override val isClosed
        @Synchronized get() = sftpConn == null

    private val serverConnectionString = ServerConnectionString(
        "sftp",
        UsernamePassword(argUsername, argPassword).encode(),
        argHostname,
        argPort
    )

    override val publicIdentifierString =
        ServerConnectionString("sftp", argUsername, argHostname, argPort).toString()

    private val sshConn = SSHClient()
    private var sftpConn: SFTPClient? = null

    init {
        try {
            sshConn.addHostKeyVerifier(PromiscuousVerifier())
            sshConn.connect(argHostname, argPort)
            sshConn.authPassword(argUsername, argPassword)
            sftpConn = sshConn.newSFTPClient()
        } catch (e: IOException) {
            sshConn.reallyClose()
        }
    }

    private fun AutoCloseable.reallyClose() {
        try {
            close()
        } catch (_: Throwable) {
        }
    }

    private inline fun <T> trySftp(block: () -> T): T? = try {
        block()
    } catch (_: SFTPException) {
        null
    } catch (_: IOException) {
        close()
        null
    }

    @Synchronized
    override fun ls(path: String): List<VfsDirEntry>? {
        if (!path.startsWith('/')) return null
        val conn = sftpConn ?: return null
        val remoteResourceInfos = trySftp { conn.ls(path) } ?: return null
        return remoteResourceInfos.mapNotNull { remoteResourceInfo ->
            if (remoteResourceInfo == null) return@mapNotNull null

            val fullPath = remoteResourceInfo.path ?: return@mapNotNull null
            val name = remoteResourceInfo.name ?: return@mapNotNull null

            object : VfsDirEntry {
                override val connectionString get() = ConnectionString(serverConnectionString, fullPath).toString()
                override val path = fullPath
                override val name = name
                override val mimeType = null
                override val isFile = remoteResourceInfo.isRegularFile
                override val isDirectory = remoteResourceInfo.isDirectory
            }
        }
    }

    private fun pathToName(path: String): String? {
        val match = pathToNameRegex.matchEntire(path) ?: return null
        return match.groupValues[1]
    }

    @Synchronized
    override fun stat(path: String): VfsStat? {
        if (!path.startsWith('/')) return null
        val conn = sftpConn ?: return null
        val fileAttributes = trySftp { conn.stat(path) } ?: return null
        return object : VfsStat {
            override val connectionString = ConnectionString(serverConnectionString, path).toString()
            override val path = path
            override val name = pathToName(path) ?: ""
            override val mimeType = null
            override val isFile = fileAttributes.type == FileMode.Type.REGULAR
            override val isDirectory = fileAttributes.type == FileMode.Type.DIRECTORY
            override val size = fileAttributes.size.toULong()
        }
    }

    @Synchronized
    override fun getInputStream(path: String): InputStream? {
        if (!path.startsWith('/')) return null
        val conn = sftpConn ?: return null
        return trySftp {
            val remoteFile = conn.open(path, setOf(OpenMode.READ)) ?: return@trySftp null
            return@trySftp remoteFile.RemoteFileInputStream()
        }
    }

    @Synchronized
    override fun close() {
        if (sftpConn == null) return

        sftpConn?.reallyClose()
        sshConn.reallyClose()

        sftpConn = null
    }

    companion object Factory : VfsFileSystemFactory<SftpFileSystem> {
        override fun build(connectionString: String): SftpFileSystem? {
            val serverConnString = ServerConnectionString.fromURI(connectionString) ?: return null
            val usernamePassword = UsernamePassword.decode(serverConnString.userInfo) ?: return null
            return SftpFileSystem(
                usernamePassword.username,
                usernamePassword.password,
                serverConnString.host,
                serverConnString.port ?: 22
            )
        }
    }
}
