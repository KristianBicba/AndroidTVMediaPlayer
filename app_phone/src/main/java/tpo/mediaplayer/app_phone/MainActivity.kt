package tpo.mediaplayer.app_phone

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.upstream.BaseDataSource
import com.google.android.exoplayer2.upstream.DataSourceException
import com.google.android.exoplayer2.upstream.DataSpec
import com.jcraft.jsch.JSch
import com.jcraft.jsch.JSchException
import com.jcraft.jsch.SftpATTRS
import com.jcraft.jsch.SftpException
import tpo.mediaplayer.app_phone.ui.theme.MediaPlayerRemoteControlTheme
import java.io.InputStream
import com.jcraft.jsch.ChannelSftp as JschChannelSftp
import com.jcraft.jsch.Session as JschSession

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MediaPlayerRemoteControlTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    VideoView(videoUri = "sftp://testuser:testpassword@10.0.2.2:2022/Screen.mkv")
                }
            }
        }
    }
}

private fun JSch.getConnectedPasswordSessionOrNull(
    username: String,
    password: String,
    host: String,
    port: UInt
): JschSession? = try {
    val session = getSession(username, host, port.toInt())
    session.setPassword(password)
    session.setConfig("StrictHostKeyChecking", "no")
    session.connect()
    session
} catch (_: JSchException) {
    null
}

private fun JschSession.openSftpChannelOrNull(): JschChannelSftp? = try {
    val channel = openChannel("sftp")
    if (channel is JschChannelSftp) {
        channel.connect()
        channel
    } else {
        null
    }
} catch (_: JSchException) {
    null
}

private fun JschChannelSftp.statOrNull(path: String): SftpATTRS? = try {
    stat(path)
} catch (_: SftpException) {
    null
}

private fun JschChannelSftp.getOrNull(path: String): InputStream? = try {
    get(path)
} catch (_: SftpException) {
    null
}

private class SftpDataSource : BaseDataSource(true) {
    init {
        println("SftpDataSource constructed")
    }

    private var uri: Uri? = null
    private var session: JschSession? = null
    private var channel: JschChannelSftp? = null
    private var inputStream: InputStream? = null

    private fun closeAndThrow(): Nothing {
        close()
        throw DataSourceException(PlaybackException.ERROR_CODE_IO_UNSPECIFIED)
    }

    override fun open(dataSpec: DataSpec): Long {
        val location = SftpLocation.fromUri(dataSpec.uri) ?: closeAndThrow()

        uri = dataSpec.uri

        val jsch = JSch()
        session = jsch.getConnectedPasswordSessionOrNull(
            location.username, location.password,
            location.host, location.port
        ) ?: closeAndThrow()
        channel = session?.openSftpChannelOrNull() ?: closeAndThrow()

        val statResult = channel?.statOrNull(location.filepath) ?: closeAndThrow()

        inputStream = channel?.getOrNull(location.filepath) ?: closeAndThrow()
        inputStream!!.skip(dataSpec.position)

        return statResult.size
    }

    override fun getUri(): Uri? = uri

    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        return (inputStream ?: closeAndThrow()).read(buffer, offset, length)
    }

    private inline fun <T> ignoreJschException(block: () -> T) = try {
        block()
    } catch (_: JSchException) {
        null
    }

    override fun close() {
        inputStream?.close()
        ignoreJschException { channel?.disconnect() }
        ignoreJschException { session?.disconnect() }

        uri = null
        session = null
        channel = null
        inputStream = null
    }

    private data class SftpLocation(
        val username: String,
        val password: String,
        val host: String,
        val port: UInt,
        val filepath: String
    ) {
        companion object {
            private val uriRegex = """sftp://(.+?):(.+?)@(.+?)(:\d+)?/(.+)""".toRegex()
            fun fromUri(uri: Uri): SftpLocation? {
                val match = uriRegex.matchEntire(uri.toString())
                    ?: throw DataSourceException(PlaybackException.ERROR_CODE_IO_UNSPECIFIED)

                var (username, password, host, port, filepath) = match.destructured
                if (port == "") port = ":22"

                port = port.removePrefix(":")

                val portNum = port.toUIntOrNull() ?: return null

                return SftpLocation(username, password, host, portNum, filepath)
            }
        }
    }
}

@Composable
private fun VideoView(videoUri: String) {
    val context = LocalContext.current

    val mediaSourceFactory = DefaultMediaSourceFactory(context)
        .setDataSourceFactory { SftpDataSource() }

    val exoPlayer = ExoPlayer.Builder(LocalContext.current)
        .setMediaSourceFactory(mediaSourceFactory)
        .build()
        .also { exoPlayer ->
            val mediaItem = MediaItem.Builder()
                .setUri(videoUri)
                .build()
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
        }

    val lifecycleOwner = rememberUpdatedState(LocalLifecycleOwner.current)

    DisposableEffect(
        AndroidView(factory = {
            StyledPlayerView(context).apply {
                player = exoPlayer
            }
        })
    ) {
        val observer = LifecycleEventObserver { owner, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    exoPlayer.pause()
                }
                Lifecycle.Event.ON_RESUME -> {
                    exoPlayer.play()
                }
                else -> {}
            }
        }
        val lifecycle = lifecycleOwner.value.lifecycle
        lifecycle.addObserver(observer)

        onDispose {
            exoPlayer.release()
            lifecycle.removeObserver(observer)
        }
    }
}

