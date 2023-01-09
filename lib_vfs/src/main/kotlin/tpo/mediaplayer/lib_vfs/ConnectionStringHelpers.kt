package tpo.mediaplayer.lib_vfs

import java.net.URI
import java.net.URISyntaxException

data class ServerConnectionString(
    val scheme: String,
    val userInfo: String,
    val host: String,
    val port: Int? = null
) {
    fun toURI() = URI(scheme, userInfo, host, port ?: -1, null, null, null)
    override fun toString() = toURI().toString()

    companion object {
        fun fromURI(uriString: String): ServerConnectionString? {
            val uri = try {
                URI(uriString).parseServerAuthority()
            } catch (_: URISyntaxException) {
                null
            } ?: return null

            if (!uri.isAbsolute || uri.isOpaque) return null

            return ServerConnectionString(
                uri.scheme ?: return null,
                uri.userInfo ?: return null,
                uri.host ?: return null,
                when (val port = uri.port) {
                    -1 -> null
                    else -> port
                }
            )
        }
    }
}

data class ConnectionString(
    val server: ServerConnectionString,
    val path: String
) {
    fun toURI() = URI(server.scheme, server.userInfo, server.host, server.port ?: -1, path, null, null)
    override fun toString() = toURI().toString()

    companion object {
        fun fromURI(uriString: String): ConnectionString? {
            val uri = try {
                URI(uriString).parseServerAuthority()
            } catch (_: URISyntaxException) {
                null
            } ?: return null

            if (!uri.isAbsolute || uri.isOpaque) return null

            return ConnectionString(
                ServerConnectionString(
                    uri.scheme ?: return null,
                    uri.userInfo ?: return null,
                    uri.host ?: return null,
                    when (val port = uri.port) {
                        -1 -> null
                        else -> port
                    }
                ),
                uri.path ?: return null
            )
        }
    }
}

data class UsernamePassword(
    val username: String,
    val password: String
) {
    fun encode(): String {
        val escapedUsername = username
            .replace("""\""", """\\""")
            .replace(""":""", """\:""")
        return "$escapedUsername:$password"
    }

    override fun toString() = encode()

    companion object {
        fun decode(input: String): UsernamePassword? {
            var isEscaped = false
            var delimPos = -1
            for (i in input.indices) {
                val c = input[i]
                if (isEscaped) isEscaped = when (c) {
                    ':' -> false
                    '\\' -> false
                    else -> return null
                } else when (c) {
                    '\\' -> isEscaped = true
                    ':' -> {
                        delimPos = i
                        break
                    }
                }
            }
            if (delimPos < 0) return null

            val escapedUsername = input.substring(0 until delimPos)
            val password = input.substring(delimPos + 1)

            return UsernamePassword(
                escapedUsername
                    .replace("""\\""", """\""")
                    .replace("""\:""", """:"""),
                password
            )
        }
    }
}