package tpo.mediaplayer.app_tv.activity

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.ui.StyledPlayerView
import tpo.mediaplayer.app_tv.R
import tpo.mediaplayer.app_tv.VfsDataSource
import tpo.mediaplayer.app_tv.VfsDataSource.VfsContainer
import tpo.mediaplayer.app_tv.service.MainServerService
import tpo.mediaplayer.app_tv.service.MainServerService.LocalBinder
import tpo.mediaplayer.lib_communications.shared.NowPlaying
import tpo.mediaplayer.lib_communications.shared.PlaybackStatus
import java.time.Instant

private val Player.isPlayingOrBuffering
    get() = playbackState == Player.STATE_BUFFERING || playbackState == Player.STATE_READY

class VideoPlayerActivity : AppCompatActivity() {
    private var serverShouldUnbind = false
    private var serverBinder: LocalBinder? = null

    private val serverConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            service as LocalBinder
            service.addListener(serverListener)
            serverBinder = service
        }

        override fun onServiceDisconnected(name: ComponentName) {
            serverBinder = null
        }
    }

    private fun serverBind() {
        val intent = Intent(this, MainServerService::class.java)
        bindService(intent, serverConnection, BIND_AUTO_CREATE)
        serverShouldUnbind = true
    }

    private fun serverUnbind() {
        if (serverShouldUnbind) {
            serverShouldUnbind = false
            unbindService(serverConnection)
        }
    }

    private val serverListener: MainServerService.Listener = object : MainServerService.Listener {
        private inline fun withReadyPlayer(block: (Player) -> Unit) {
            val player = player ?: return
            if (playbackStatus !is PlaybackStatus.Playing) return
            block(player)
        }

        override fun onPauseRequest() = withReadyPlayer { player ->
            player.pause()
        }

        override fun onResumeRequest() = withReadyPlayer { player ->
            player.play()
        }

        override fun onStopRequest() {
            if (playbackStatus is PlaybackStatus.Error) {
                updatePlaybackStatus(PlaybackStatus.Idle)
            } else withReadyPlayer { player ->
                player.stop()
            }
        }

        override fun onSeekRequest(newTimeElapsed: Long) = withReadyPlayer { player ->
            player.seekTo(newTimeElapsed)
        }
    }

    private lateinit var mediaSourceFactory: MediaSource.Factory
    private val vfsContainer = VfsContainer(null, null)

    private var player: ExoPlayer? = null
    private var playbackStatus: PlaybackStatus = PlaybackStatus.Idle

    private val playerListener: Player.Listener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            when {
                events.contains(Player.EVENT_PLAYER_ERROR) ->
                    updatePlaybackStatus(PlaybackStatus.Error(player.playerError.toString()))
                events.contains(Player.EVENT_POSITION_DISCONTINUITY) ->
                    if (player.isPlayingOrBuffering)
                        updatePlaybackStatusSeek(player.isPlaying, player.currentPosition)
                events.contains(Player.EVENT_PLAYBACK_STATE_CHANGED)
                        && player.isPlaying
                        && playbackStatus !is PlaybackStatus.Playing -> {
                    val mediaInfo = NowPlaying.MediaInfo(
                        vfsContainer.vfsUri!!,
                        vfsContainer.stat!!.path,
                        vfsContainer.stat!!.name,
                        player.duration
                    )
                    updatePlaybackStatusStartPlayback(mediaInfo, player.isPlaying)
                }
                events.contains(Player.EVENT_PLAYBACK_STATE_CHANGED)
                        && !player.isPlayingOrBuffering
                        && playbackStatus is PlaybackStatus.Playing -> {
                    updatePlaybackStatus(PlaybackStatus.Idle)
                    finish()
                }
                events.contains(Player.EVENT_IS_PLAYING_CHANGED) -> {
                    updatePlaybackStatusSeek(player.isPlaying, player.currentPosition)
                }
            }
        }
    }

    private fun updatePlaybackStatus(update: PlaybackStatus) {
        playbackStatus = update
        if (serverBinder != null) serverBinder!!.updatePlaybackStatus(update)
    }

    private fun updatePlaybackStatusSeek(isPlaying: Boolean, position: Long) {
        val playbackStatus = playbackStatus as? PlaybackStatus.Playing ?: return
        updatePlaybackStatus(
            PlaybackStatus.Playing(
                playbackStatus.data.copy(
                    timeElapsed = position,
                    timeUpdated = Instant.now(),
                    status = if (isPlaying) NowPlaying.Status.PLAYING else NowPlaying.Status.PAUSED
                )
            )
        )
    }

    private fun updatePlaybackStatusStartPlayback(mediaInfo: NowPlaying.MediaInfo, isPlaying: Boolean) {
        updatePlaybackStatus(
            PlaybackStatus.Playing(
                NowPlaying(
                    mediaInfo,
                    0,
                    Instant.now(),
                    if (isPlaying) NowPlaying.Status.PLAYING else NowPlaying.Status.PAUSED
                )
            )
        )
    }

    private fun initializePlayer() {
        releasePlayer()

        val player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
            .also { this.player = it }
        player.addListener(playerListener)

        findViewById<StyledPlayerView>(R.id.idExoPlayerVIew).player = player
    }

    private fun releasePlayer() {
        val player = player ?: return

        player.stop()
        findViewById<StyledPlayerView>(R.id.idExoPlayerVIew).player = null
        player.release()

        this.player = null
    }

    private fun playMedia(uri: String) {
        val player = player ?: return
        val videoUri = Uri.parse(uri)
        val mediaItem = MediaItem.fromUri(videoUri)

        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.video_player)

        mediaSourceFactory = DefaultMediaSourceFactory(this)
            .setDataSourceFactory { VfsDataSource(vfsContainer) }
    }

    override fun onStart() {
        super.onStart()
        val uri = intent.getStringExtra("uri") ?: kotlin.run { finish(); return }
        serverBind()
        initializePlayer()
        playMedia(uri)
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
        serverUnbind()
    }
}