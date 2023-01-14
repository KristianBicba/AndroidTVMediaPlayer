package tpo.mediaplayer.app_phone.activity

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import com.google.android.material.slider.Slider
import tpo.mediaplayer.app_phone.AbstractBinder
import tpo.mediaplayer.app_phone.GodObject
import tpo.mediaplayer.app_phone.R
import tpo.mediaplayer.app_phone.db.Device
import tpo.mediaplayer.app_phone.db.inetAddress
import tpo.mediaplayer.app_phone.service.ClientService
import tpo.mediaplayer.lib_communications.shared.NowPlaying
import tpo.mediaplayer.lib_communications.shared.PlaybackStatus

class RemoteControlActivity : AppCompatActivity() {
    private lateinit var vDeviceName: TextView
    private lateinit var vMediaName: TextView
    private lateinit var vButtonStop: Button
    private lateinit var vButtonPlay: Button
    private lateinit var vButtonPause: Button
    private lateinit var vSeekbar: Slider
    private lateinit var vSeekbarLabel: TextView
    private lateinit var vButtonOpen: Button

    private var textPrimaryColor: Int = 0

    private var intentUid = 0
    private lateinit var device: Device

    private val listener = object : ClientService.Listener {
        override fun onOpen() {
            clientCanStop = false
        }

        override fun onUpdateNowPlaying(newValue: PlaybackStatus) {
            updateNowPlaying(newValue)
        }

        override fun onClose(error: String?) {
            if (!clientCanStop) finishWithError(error ?: "Client closed unexpectedly")
        }
    }

    private val client =
        object : AbstractBinder<ClientService.LocalBinder>(this, ClientService::class.java) {
            override fun onBind(binder: ClientService.LocalBinder) {
                binder.addListener(listener)
                startClient(binder)
            }

            override fun onUnbind(binder: ClientService.LocalBinder?) {
                binder?.removeListener(listener)
            }
        }

    private var clientCanStop = false

    private fun startClient(binder: ClientService.LocalBinder) {
        val inetAddress = device.inetAddress
            ?: return finishWithError("Device database entry is corrupt")
        binder.createClient(inetAddress)
    }

    private fun disableControls() {
        vDeviceName.text = "..."
        vMediaName.visibility = View.GONE
        vMediaName.text = ""
        vMediaName.setTextColor(textPrimaryColor)
        vButtonStop.isEnabled = false
        vButtonPlay.isEnabled = false
        vButtonPause.isEnabled = false
        vSeekbar.isEnabled = false
        vSeekbar.value = 0.0f
        vSeekbarLabel.text = "??/??"
        vButtonOpen.isEnabled = false
    }

    private fun idleControls() {
        vDeviceName.text = device.name
        vMediaName.visibility = View.GONE
        vButtonStop.isEnabled = false
        vButtonPlay.isEnabled = false
        vButtonPause.isEnabled = false
        vSeekbar.isEnabled = false
        vSeekbar.value = 0.0f
        vSeekbarLabel.text = "??/??"
        vButtonOpen.isEnabled = true
    }

    private fun playingControls(playing: NowPlaying) {
        vDeviceName.text = device.name
        vMediaName.visibility = View.VISIBLE
        vMediaName.text = playing.mediaInfo.mediaName
        vMediaName.setTextColor(textPrimaryColor)
        vButtonStop.isEnabled = true
        vButtonPlay.isEnabled = playing.status == NowPlaying.Status.PAUSED
        vButtonPause.isEnabled = playing.status == NowPlaying.Status.PLAYING
        vSeekbar.isEnabled = true
        vSeekbar.value = 0.0f
        vSeekbarLabel.text = "??/??"
        vButtonOpen.isEnabled = true
    }

    private fun errorControls(error: String) {
        vDeviceName.text = device.name
        vMediaName.visibility = View.VISIBLE
        vMediaName.text = error
        vMediaName.setTextColor(0xFFE44141.toColorInt())
        vButtonStop.isEnabled = false
        vButtonPlay.isEnabled = false
        vButtonPause.isEnabled = false
        vSeekbar.isEnabled = false
        vSeekbar.value = 0.0f
        vSeekbarLabel.text = "??/??"
        vButtonOpen.isEnabled = true
    }

    private fun finishWithError(error: String) {
        Toast.makeText(this, "Device disconnected: $error", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun updateNowPlaying(newValue: PlaybackStatus) {
        when (newValue) {
            is PlaybackStatus.Playing -> playingControls(newValue.data)
            is PlaybackStatus.Error -> errorControls(newValue.error)
            is PlaybackStatus.Idle -> idleControls()
        }
    }

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

        textPrimaryColor = vMediaName.currentTextColor

        intentUid = intent.getIntExtra("server_uid", 0)
        if (intentUid == 0) return finishWithError("No server_uid given")
        device = GodObject.instance.db.deviceDao().getDeviceByUid(intentUid)
            ?: return finishWithError("server_uid is invalid")

        disableControls()

        vButtonStop.setOnClickListener { client.binder?.stop() }
        vButtonPlay.setOnClickListener { client.binder?.play() }
        vButtonPause.setOnClickListener { client.binder?.pause() }
    }

    override fun onStart() {
        super.onStart()
        clientCanStop = false
        client.bind()
    }

    override fun onStop() {
        super.onStop()
        clientCanStop = true
        client.binder?.stopClient()
        client.unbind()
    }
}