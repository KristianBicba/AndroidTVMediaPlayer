package tpo.mediaplayer.app_phone.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.slider.Slider;

import tpo.mediaplayer.app_phone.R;
import tpo.mediaplayer.app_phone.Television;
import tpo.mediaplayer.lib_communications.client.Client;

public class RemoteControlActivity extends AppCompatActivity {

    static Television televizija = null;

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

        TextView sliderText = findViewById(R.id.textViewSlider);
        Slider sliderBar = findViewById(R.id.sliderBar);

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

        televizija.addSlider(sliderBar);

        sliderBar.addOnChangeListener(new Slider.OnChangeListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                sliderText.setText(Float.toString(value));
            }
        });

        televizija.updateSlider();

    }
}