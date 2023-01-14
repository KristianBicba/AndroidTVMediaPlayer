package tpo.mediaplayer.app_phone.activity

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.slider.Slider
import tpo.mediaplayer.app_phone.R

class RemoteControlActivity : AppCompatActivity() {
    private lateinit var vDeviceName: TextView
    private lateinit var vMediaName: TextView
    private lateinit var vButtonStop: Button
    private lateinit var vButtonPlay: Button
    private lateinit var vButtonPause: Button
    private lateinit var vSeekbar: Slider
    private lateinit var vSeekbarLabel: TextView
    private lateinit var vButtonOpen: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_remote_control)

        vDeviceName = findViewById(R.id.remote_control_text_device)
        vMediaName = findViewById(R.id.remote_control_text_media)
        vButtonStop = findViewById(R.id.remote_control_button_stop)
        vButtonPlay = findViewById(R.id.remote_control_button_play)
        vButtonPause = findViewById(R.id.remote_control_button_pause)
        vSeekbar = findViewById(R.id.remote_control_slider_seekbar)
        vSeekbarLabel = findViewById(R.id.remote_control_text_seekbar)
        vButtonOpen = findViewById(R.id.remote_control_button_open)
    }
}