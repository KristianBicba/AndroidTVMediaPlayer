package tpo.mediaplayer.app_phone;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class RemoteControlActivity extends AppCompatActivity {

    static Television televizija = ConnectActivity.getTelevision();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote_control);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(televizija.currentMovie);

        Button play =  findViewById(R.id.buttonPlay);
        Button pause =  findViewById(R.id.buttonPause);
        Button stop =  findViewById(R.id.buttonStop);
        Button volumeUp =  findViewById(R.id.buttonVolumeUp);
        Button volumeDown =  findViewById(R.id.buttonVolumeDown);
        Button subtitles =  findViewById(R.id.buttonSubtitles);
        Button options =  findViewById(R.id.buttonOptions);

        Button back = findViewById(R.id.buttonBack);

        play.setOnClickListener(v ->
        {
            televizija.play();
        });

        pause.setOnClickListener(v ->
        {
            televizija.pause();
        });

        stop.setOnClickListener(v ->
        {
            televizija.stop();
        });

        volumeUp.setOnClickListener(v ->
        {
            televizija.volumeUp();
        });

        volumeDown.setOnClickListener(v ->
        {
            televizija.volumeDown();
        });

        subtitles.setOnClickListener(v ->
        {
            televizija.subtitles();
        });

        options.setOnClickListener(v ->
        {
            televizija.options();
        });

        back.setOnClickListener(v ->
        {
            startActivity(new Intent(RemoteControlActivity.this, FileSystemActivity.class));
        });
    }
}