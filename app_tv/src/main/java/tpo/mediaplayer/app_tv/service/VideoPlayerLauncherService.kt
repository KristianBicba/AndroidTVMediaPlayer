package tpo.mediaplayer.app_tv.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import androidx.lifecycle.LifecycleService
import tpo.mediaplayer.app_tv.activity.VideoPlayerActivity

class VideoPlayerLauncherService : LifecycleService() {
    private lateinit var mainServerBinder: MainServerService.LocalBinder

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            mainServerBinder = service as MainServerService.LocalBinder
            mainServerBinder.addListener(listener)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            mainServerBinder.removeListener(listener)
        }
    }

    private lateinit var handler: Handler

    private val listener = object : MainServerService.Listener {
        override fun onPlayRequest(connectionString: String) {
            launchMainActivity(connectionString)
        }
    }

    private fun launchMainActivity(connectionString: String) {
        val intent = Intent(this, VideoPlayerActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra("uri", connectionString)
        }
        startActivity(intent)
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

    private val binder = object : Binder() {}

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    override fun onDestroy() {
        unbindService(connection)
        super.onDestroy()
    }
}