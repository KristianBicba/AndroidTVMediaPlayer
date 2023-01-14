package tpo.mediaplayer.app_phone.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import tpo.mediaplayer.app_phone.GodObject
import tpo.mediaplayer.lib_communications.client.Client
import tpo.mediaplayer.lib_communications.client.ClientCallbacks
import tpo.mediaplayer.lib_communications.shared.PlaybackStatus
import java.io.IOException
import java.net.InetAddress
import java.time.Instant

class ClientService : Service() {
    interface Listener {
        fun onOpen() {}
        fun onUpdateNowPlaying(newValue: PlaybackStatus, serverTime: Instant?) {}
        fun onClose(error: String?) {}
    }

    private lateinit var handler: Handler
    private val listeners = mutableListOf<Listener>()

    inner class LocalBinder : Binder() {
        fun addListener(listener: Listener) {
            listeners += listener
        }

        fun removeListener(listener: Listener) {
            listeners -= listener
        }

        fun createClient(address: InetAddress) {
            Thread {
                this@ClientService.createClient(address)
            }.start()
        }

        fun stopClient() {
            this@ClientService.stopClient()
        }

        fun play(connectionString: String) {
            client?.beginPlayback(connectionString)
        }

        fun play() {
            client?.resumePlayback()
        }

        fun pause() {
            client?.pausePlayback()
        }

        fun stop() {
            client?.stopPlayback()
        }

        fun seek(position: Long) {
            client?.seekPlayback(position)
        }
    }

    private val binder = LocalBinder()

    private var client: Client? = null

    @Synchronized
    private fun createClient(address: InetAddress) {
        stopClient()
        try {
            val client = Client(object : ClientCallbacks {
                override fun onUpdateNowPlaying(newValue: PlaybackStatus, serverTime: Instant?) {
                    handler.post {
                        listeners.forEach { it.onUpdateNowPlaying(newValue, serverTime) }
                    }
                }

                override fun onClose(error: Throwable?) {
                    handler.post {
                        listeners.forEach { it.onClose(error?.toString()) }
                        client = null
                    }
                }
            }, address)
            this.client = client

            val error = client.establish(GodObject.instance.guid)
            if (error != null) {
                handler.post {
                    if (this.client == null) return@post
                    listeners.forEach { it.onClose(error) }
                    this.client = null
                }
            } else {
                handler.post {
                    listeners.forEach { it.onOpen() }
                }
            }
        } catch (e: IOException) {
            handler.post {
                if (client == null) return@post
                listeners.forEach { it.onClose(e.toString()) }
                client = null
            }
        }
    }

    @Synchronized
    private fun stopClient() {
        client?.apply {
            close()
            client = null
        }
    }

    override fun onCreate() {
        handler = Handler(mainLooper)
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        listeners.clear()
        stopClient()
        return false
    }
}
