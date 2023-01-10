package tpo.mediaplayer.app_tv.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.ui.StyledPlayerView;

import java.time.Instant;

import tpo.mediaplayer.app_tv.R;
import tpo.mediaplayer.app_tv.VfsDataSource;
import tpo.mediaplayer.app_tv.service.MainServerService;
import tpo.mediaplayer.lib_communications.shared.NowPlaying;
import tpo.mediaplayer.lib_communications.shared.PlaybackStatus;

public class VideoPlayer extends AppCompatActivity {
    private MediaSource.Factory mediaSourceFactory;
    private ExoPlayer player = null;
    private MainServerService.LocalBinder binder = null;
    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder = (MainServerService.LocalBinder) service;
            lastConnectionString = null;
            lastTimeElapsed = 0;
            lastPaused = false;
            binder.getClientRequestedPlayback().observe(VideoPlayer.this, observer);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            binder.getClientRequestedPlayback().removeObserver(observer);
        }
    };

    private String lastConnectionString = null;
    private long lastTimeElapsed = 0;
    private boolean lastPaused = false;
    private final Observer<MainServerService.ClientRequestedPlayback> observer = clientRequestedPlayback -> {
        if (player != null) {
            if (clientRequestedPlayback == null) {
                if (lastConnectionString != null) {
                    player.stop();
                    finish();
                }
            } else {
                if (lastConnectionString == null) {
                    playMedia(clientRequestedPlayback.getConnectionString());
                }
                if (lastTimeElapsed != clientRequestedPlayback.getTimeElapsed()) {
                    // TODO
                }
                if (lastPaused != clientRequestedPlayback.getPaused()) {
                    if (clientRequestedPlayback.getPaused()) {
                        player.pause();
                    } else {
                        player.play();
                    }
                }
            }
        }

        if (clientRequestedPlayback == null) {
            lastConnectionString = null;
            lastTimeElapsed = 0;
            lastPaused = false;
        } else {
            lastConnectionString = clientRequestedPlayback.getConnectionString();
            lastTimeElapsed = clientRequestedPlayback.getTimeElapsed();
            lastPaused = clientRequestedPlayback.getPaused();
        }
    };

    private final VfsDataSource.VfsContainer vfsContainer = new VfsDataSource.VfsContainer(null, null);
    private PlaybackStatus playbackStatus = PlaybackStatus.Idle.INSTANCE;
    private final Player.Listener playerListener = new Player.Listener() {
        @Override
        public void onPlaybackStateChanged(int playbackState) {
            if (playbackState == Player.STATE_READY && !(playbackStatus instanceof PlaybackStatus.Playing)) {
                updatePlaybackStatus(new PlaybackStatus.Playing(new NowPlaying(
                        new NowPlaying.MediaInfo(
                                vfsContainer.getVfsUri(),
                                vfsContainer.getStat().getPath(),
                                vfsContainer.getStat().getName(),
                                player.getDuration()
                        ),
                        0,
                        Instant.now(),
                        NowPlaying.Status.PLAYING
                )));
            } else if ((playbackState == Player.STATE_ENDED || playbackState == Player.STATE_IDLE) &&
                    playbackStatus instanceof PlaybackStatus.Playing) {
                updatePlaybackStatus(PlaybackStatus.Idle.INSTANCE);
            }
        }

        @Override
        public void onIsPlayingChanged(boolean isPlaying) {
            if (player.getPlaybackState() == Player.STATE_IDLE || player.getPlaybackState() == Player.STATE_ENDED)
                return;
            if (!(playbackStatus instanceof PlaybackStatus.Playing)) return;
            updatePlaybackStatus(new PlaybackStatus.Playing(new NowPlaying(
                    ((PlaybackStatus.Playing) playbackStatus).getData().getMediaInfo(),
                    player.getCurrentPosition(),
                    Instant.now(),
                    isPlaying ? NowPlaying.Status.PLAYING : NowPlaying.Status.PAUSED
            )));
        }

        @Override
        public void onPlayerError(@NonNull PlaybackException error) {
            updatePlaybackStatus(new PlaybackStatus.Error(error.toString()));
        }

        @Override
        public void onPositionDiscontinuity(
                @NonNull Player.PositionInfo oldPosition,
                @NonNull Player.PositionInfo newPosition,
                int reason
        ) {
            if (player.getPlaybackState() == Player.STATE_IDLE || player.getPlaybackState() == Player.STATE_ENDED)
                return;
            if (!(playbackStatus instanceof PlaybackStatus.Playing)) return;
            updatePlaybackStatus(new PlaybackStatus.Playing(new NowPlaying(
                    ((PlaybackStatus.Playing) playbackStatus).getData().getMediaInfo(),
                    player.getCurrentPosition(),
                    Instant.now(),
                    player.isPlaying() ? NowPlaying.Status.PLAYING : NowPlaying.Status.PAUSED
            )));
        }
    };

    private void updatePlaybackStatus(PlaybackStatus update) {
        playbackStatus = update;
        binder.updatePlaybackStatus(update);
    }

    private void initializePlayer() {
        player = new ExoPlayer.Builder(this)
                .setMediaSourceFactory(mediaSourceFactory)
                .build();

        player.addListener(playerListener);

        StyledPlayerView playerView = findViewById(R.id.idExoPlayerVIew);
        playerView.setPlayer(player);
    }

    private void releasePlayer() {
        if (player == null) return;

        player.stop();

        StyledPlayerView playerView = findViewById(R.id.idExoPlayerVIew);
        playerView.setPlayer(null);

        player.release();
        player = null;
    }

    private void playMedia(String uri) {
        Uri videoUri = Uri.parse(uri);
        MediaItem mediaItem = MediaItem.fromUri(videoUri);
        player.setMediaItem(mediaItem);
        player.prepare();
        player.play();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_player);

        mediaSourceFactory = new DefaultMediaSourceFactory(this)
                .setDataSourceFactory(() -> new VfsDataSource(vfsContainer));
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent serverBindIntent = new Intent(this, MainServerService.class);
        bindService(serverBindIntent, connection, Context.BIND_AUTO_CREATE);

        initializePlayer();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(connection);

        releasePlayer();
    }
}
