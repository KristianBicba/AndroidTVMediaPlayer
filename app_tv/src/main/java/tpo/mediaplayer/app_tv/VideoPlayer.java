package tpo.mediaplayer.app_tv;


import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.StyledPlayerView;


public class VideoPlayer extends AppCompatActivity {


    private ExoPlayer player;
    StyledPlayerView playerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_player);
        playerView = findViewById(R.id.idExoPlayerVIew);

        Context context = this;
        player = new ExoPlayer.Builder(context).build();
        playerView.setPlayer(player);


        Uri videoUri = Uri.parse("https://media.geeksforgeeks.org/wp-content/uploads/20201217163353/Screenrecorder-2020-12-17-16-32-03-350.mp4");

        MediaItem mediaItem = MediaItem.fromUri(videoUri);

        player.setMediaItem(mediaItem);

        player.prepare();

        player.play();



    }



    //private MediaSorce Prepere_source(Uri videouri){}


}




