package tpo.mediaplayer.lib_vfs.sftp

import kotlin.test.Ignore
import kotlin.test.Test

internal class SftpFileSystemTest {
    @Test
    @Ignore
    fun exampleSftp() {
        val fs = SftpFileSystem.build("sftp://testuser:testpassword@127.0.0.1:2022")
        fs?.ls("/")?.forEach {
            println(it.path)
            println(it.connectionString)
            fs.ls(it.path)?.forEach { iit ->
                println(iit.path)
                println(iit.connectionString)
            }
        }
        println(fs?.publicIdentifierString)
        println(fs?.getInputStream("/test/a.txt")?.use { it.readAllBytes().decodeToString() })
    }
}