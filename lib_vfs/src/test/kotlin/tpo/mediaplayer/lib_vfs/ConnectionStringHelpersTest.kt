package tpo.mediaplayer.lib_vfs

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class ConnectionStringHelpersTest {
    @Test
    fun exampleConnectionString() {
        val conn = ServerConnectionString("sftp", "testuser:testpassword", "10.0.2.2", null)
        println(conn.toURI())
        println(ConnectionString.fromURI("sftp://testuser:testpassword@10.0.2.2:2022/Screen.mkv"))
        println(ConnectionString(conn, "/Screen.mkv"))
    }

    fun testUsernamePassword(username: String, password: String) {
        val userPass = UsernamePassword(username, password)
        assertEquals(username, userPass.username)
        assertEquals(password, userPass.password)
        val encoded = userPass.encode()
        println(encoded)
        val decoded = UsernamePassword.decode(encoded)
        assertNotNull(decoded)
        assertEquals(username, decoded.username)
        assertEquals(password, decoded.password)
    }

    @Test
    fun testUsernamePassword() {
        testUsernamePassword("testuser", "testpassword")
        testUsernamePassword("", "testpassword")
        testUsernamePassword("testuser", "")
        testUsernamePassword("test:user", "test:password")
        testUsernamePassword("\\:\\\\\\\\:", "::::")
    }
}