package tpo.mediaplayer.app_tv.service

import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.view.accessibility.AccessibilityEvent

class KeepaliveService : AccessibilityService() {
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            println("Video player launcher connected")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            println("Video player launcher disconnected")
        }

    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        println("Accessibility service connected")

        Intent(this, VideoPlayerLauncherService::class.java).also {
            bindService(it, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
    }

    override fun onInterrupt() {
        println("Accessibility service interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        println("Accessibility service destroyed")
        unbindService(connection)
    }
}