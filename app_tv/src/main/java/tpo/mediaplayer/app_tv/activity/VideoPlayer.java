package tpo.mediaplayer.app_tv.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.ui.StyledPlayerView;

import tpo.mediaplayer.app_tv.R;
import tpo.mediaplayer.app_tv.VfsDataSource;
import tpo.mediaplayer.app_tv.service.MainServerService;

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

    private void initializePlayer() {
        player = new ExoPlayer.Builder(this)
                .setMediaSourceFactory(mediaSourceFactory)
                .build();

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
                .setDataSourceFactory(VfsDataSource::new);
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
