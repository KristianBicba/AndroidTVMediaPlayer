package tpo.mediaplayer.app_tv.service

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.Observer
import tpo.mediaplayer.app_tv.activity.VideoPlayer

class VideoPlayerLauncherService : LifecycleService() {
    private lateinit var mainServerBinder: MainServerService.LocalBinder

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            mainServerBinder = service as MainServerService.LocalBinder
            mainServerBinder.clientRequestedPlayback.observe(this@VideoPlayerLauncherService, observer)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            mainServerBinder.clientRequestedPlayback.removeObserver(observer)
        }
    }

    private lateinit var handler: Handler

    private var lastRequestedConnectionString: String? = null
    private val observer = Observer<MainServerService.ClientRequestedPlayback?> {
        if (lastRequestedConnectionString == null && it != null) {
            lastRequestedConnectionString = it.connectionString
            handler.post {
                launchMainActivity()
            }
        }
        lastRequestedConnectionString = it?.connectionString
    }

    private fun launchMainActivity() {
        val switchActivityIntent = Intent(this, VideoPlayer::class.java)
        switchActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(switchActivityIntent)
    }

    override fun onCreate() {
        super.onCreate()
        handler = Handler(mainLooper)
        val intent = Intent(this, MainServerService::class.java)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    override fun onDestroy() {
        unbindService(connection)
        super.onDestroy()
    }
}