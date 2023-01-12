package tpo.mediaplayer.app_tv.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import tpo.mediaplayer.app_tv.Device
import tpo.mediaplayer.app_tv.GodObject
import tpo.mediaplayer.lib_communications.server.Server
import tpo.mediaplayer.lib_communications.server.ServerCallbacks
import tpo.mediaplayer.lib_communications.shared.PlaybackStatus

class MainServerService : Service() {
    interface Listener {
        fun onPlayRequest(connectionString: String) {}
        fun onPauseRequest() {}
        fun onResumeRequest() {}
        fun onStopRequest() {}
        fun onSeekRequest(newTimeElapsed: Long) {}
    }

    private lateinit var handler: Handler
    private val listeners = mutableSetOf<Listener>()

    inner class LocalBinder : Binder() {
        fun addListener(listener: Listener) {
            listeners += listener
        }

        fun removeListener(listener: Listener) {
            listeners -= listener
        }

        fun updatePlaybackStatus(playbackStatus: PlaybackStatus) {
            server.updateNowPlaying(playbackStatus)
        }

        fun setPairing(allowPairing: Boolean): ByteArray? {
            if (!allowPairing) {
                server.cancelPairing()
                return null
            } else {
                val pairingData = server.beginPairing() ?: return null
                return pairingData.toByteArray()
            }
        }
    }

    private val binder = LocalBinder()

    private val server = Server(object : ServerCallbacks {
        override fun onOpen(server: Server) {
            println("Server opening")
        }

        override fun onPairingRequest(clientName: String, clientGuid: String): String? {
            val dao = GodObject.INSTANCE.db.deviceDao() ?: return "Unable to get DAO"
            if (dao.getByGuid(clientGuid) != null) {
                return "GUID is already paired"
            } else {
                dao.insert(Device().apply {
                    deviceName = clientName
                    communicationStr = clientGuid
                })
                return null
            }
        }

        override fun onConnectionRequest(clientGuid: String): String? {
            if (clientGuid == "test") return null // Allow test code to interact with the server

            val dao = GodObject.INSTANCE.db.deviceDao() ?: return "Unable to get DAO"
            if (dao.getByGuid(clientGuid) == null) {
                return "Unknown device GUID"
            } else {
                return null
            }
        }

        override fun onPlayRequest(connectionString: String) {
            handler.post {
                listeners.forEach { it.onPlayRequest(connectionString) }
            }
        }

        override fun onPauseRequest() {
            handler.post {
                listeners.forEach { it.onPauseRequest() }
            }
        }

        override fun onResumeRequest() {
            handler.post {
                listeners.forEach { it.onResumeRequest() }
            }
        }

        override fun onStopRequest() {
            handler.post {
                listeners.forEach { it.onStopRequest() }
            }
        }

        override fun onSeekRequest(newTimeElapsed: Long) {
            handler.post {
                listeners.forEach { it.onSeekRequest(newTimeElapsed) }
            }
        }

        override fun onClose(error: Throwable?) {
            println("Server closing")
        }
    })

    override fun onCreate() {
        handler = Handler(mainLooper)
    }

    override fun onBind(intent: Intent?): IBinder {
        server.open()
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        server.close()
        return false
    }
}