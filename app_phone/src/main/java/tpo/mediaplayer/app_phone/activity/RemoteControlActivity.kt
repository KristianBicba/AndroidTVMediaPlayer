package tpo.mediaplayer.app_phone.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
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
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

private fun formatMillis(millis: Long): String {
    val totalSeconds = millis / 1000
    val totalMinutes = totalSeconds / 60
    val totalHours = totalMinutes / 60
    return "%02d:%02d:%02d".format(totalHours, totalMinutes % 60, totalSeconds % 60)
}

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

        override fun onUpdateNowPlaying(newValue: PlaybackStatus, serverTime: Instant?) {
            updateNowPlaying(newValue, serverTime)
        }

        override fun onClose(error: String?) {
            abortSeekbarTask()
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

    private var latestPlaybackStatus: PlaybackStatus? = null
    private var dragPaused = false
    private var isDraggingSlider = false

    private val handler by lazy { Handler(mainLooper) }

    private val seekbarTimer = Timer(true)
    private var seekbarTask: TimerTask? = null

    private fun rescheduleSeekbarTask(elapsed: Long, total: Long, base: Instant = Instant.now()) {
        seekbarTimer.purge()
        seekbarTask = object : TimerTask() {
            val base = base

            override fun run() {
                handler.post {
                    if (seekbarTask != this) {
                        cancel()
                        return@post
                    }

                    val sinceBase = Duration.between(this.base, Instant.now()).toMillis()
                    val nowElapsed = elapsed + sinceBase

                    updateTimer(nowElapsed, total)
                }
            }
        }
        val nextSecond = Instant.now().plusSeconds(1).truncatedTo(ChronoUnit.SECONDS)
        seekbarTimer.scheduleAtFixedRate(seekbarTask, Date.from(nextSecond), 100)
    }

    private fun abortSeekbarTask() {
        seekbarTimer.purge()
        seekbarTask = null
    }

    private fun disableControls() {
        abortSeekbarTask()
        vDeviceName.text = "..."
        vMediaName.visibility = View.GONE
        vMediaName.text = ""
        vMediaName.setTextColor(textPrimaryColor)
        vButtonStop.isEnabled = false
        vButtonPlay.isEnabled = false
        vButtonPause.isEnabled = false
        vSeekbar.isEnabled = false
        vSeekbar.value = 0.0f
        vSeekbarLabel.text = "?"
        vButtonOpen.isEnabled = false
    }

    private fun idleControls() {
        abortSeekbarTask()
        vDeviceName.text = device.name
        vMediaName.visibility = View.GONE
        vButtonStop.isEnabled = false
        vButtonPlay.isEnabled = false
        vButtonPause.isEnabled = false
        vSeekbar.isEnabled = false
        vSeekbar.value = 0.0f
        vSeekbarLabel.text = "?"
        vButtonOpen.isEnabled = true
    }

    @SuppressLint("SetTextI18n")
    private fun updateTimer(elapsed: Long, total: Long) {
        val fraction = elapsed.toDouble() / total.toDouble()
        if (!isDraggingSlider || latestPlaybackStatus !is PlaybackStatus.Playing)
            vSeekbar.value = fraction.toFloat()
        vSeekbarLabel.text = "${formatMillis(elapsed)}/${formatMillis(total)}"
    }

    private fun playingControls(playing: NowPlaying, serverTime: Instant?) {
        vDeviceName.text = device.name
        vMediaName.visibility = View.VISIBLE
        vMediaName.text = playing.mediaInfo.mediaName
        vMediaName.setTextColor(textPrimaryColor)
        vButtonStop.isEnabled = true
        vButtonPlay.isEnabled = playing.status == NowPlaying.Status.PAUSED
        vButtonPause.isEnabled = playing.status == NowPlaying.Status.PLAYING
        vSeekbar.isEnabled = true
        vButtonOpen.isEnabled = true

        updateTimer(playing.timeElapsed, playing.mediaInfo.timeTotal)
        if (playing.status == NowPlaying.Status.PLAYING) {
            rescheduleSeekbarTask(
                playing.timeElapsed,
                playing.mediaInfo.timeTotal,
                if (serverTime != null) playing.timeUpdated.plus(Duration.between(serverTime, Instant.now()))
                else playing.timeUpdated
            )
        } else {
            abortSeekbarTask()
        }
    }

    private fun errorControls(error: String) {
        abortSeekbarTask()
        vDeviceName.text = device.name
        vMediaName.visibility = View.VISIBLE
        vMediaName.text = error
        vMediaName.setTextColor(0xFFE44141.toColorInt())
        vButtonStop.isEnabled = false
        vButtonPlay.isEnabled = false
        vButtonPause.isEnabled = false
        vSeekbar.isEnabled = false
        vSeekbar.value = 0.0f
        vSeekbarLabel.text = "?"
        vButtonOpen.isEnabled = true
    }

    private fun finishWithError(error: String) {
        Toast.makeText(this, "Device disconnected: $error", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun updateNowPlaying(newValue: PlaybackStatus, serverTime: Instant?) {
        latestPlaybackStatus = newValue
        when (newValue) {
            is PlaybackStatus.Playing -> playingControls(newValue.data, serverTime)
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

        vSeekbar.setLabelFormatter {
            val total = (latestPlaybackStatus as? PlaybackStatus.Playing)?.data?.mediaInfo?.timeTotal
                ?: return@setLabelFormatter "ERROR"
            val wouldBeElapsed = (total * it).toLong()
            formatMillis(wouldBeElapsed)
        }

        vSeekbar.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                isDraggingSlider = true
                dragPaused = latestPlaybackStatus.let {
                    it is PlaybackStatus.Playing && it.data.status == NowPlaying.Status.PLAYING
                }
                if (dragPaused) {
                    client.binder?.pause()
                }
            }

            override fun onStopTrackingTouch(slider: Slider) {
                isDraggingSlider = false
                val total = (latestPlaybackStatus as? PlaybackStatus.Playing)?.data?.mediaInfo?.timeTotal
                    ?: return
                val wouldBeElapsed = (total * slider.value).toLong()
                client.binder?.seek(wouldBeElapsed)
                if (dragPaused) {
                    client.binder?.play()
                }
            }
        })
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