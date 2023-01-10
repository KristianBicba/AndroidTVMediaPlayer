package tpo.mediaplayer.app_tv.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import tpo.mediaplayer.lib_communications.server.Server
import tpo.mediaplayer.lib_communications.server.ServerCallbacks
import tpo.mediaplayer.lib_communications.shared.PlaybackStatus

class MainServerService : Service() {
    data class ClientRequestedPlayback(
        val connectionString: String,
        val timeElapsed: Long,
        val paused: Boolean
    )

    var clientRequestedPlayback: ClientRequestedPlayback? = null
    val clientRequestedPlaybackMLD = MutableLiveData<ClientRequestedPlayback?>(null)

    inner class LocalBinder : Binder() {
        val clientRequestedPlayback: LiveData<ClientRequestedPlayback?> = clientRequestedPlaybackMLD
        fun updatePlaybackStatus(playbackStatus: PlaybackStatus) {
            if (playbackStatus is PlaybackStatus.Idle || playbackStatus is PlaybackStatus.Error) {
                clientRequestedPlaybackMLD.postValue(null)
            }
            server.updateNowPlaying(playbackStatus)
        }
    }

    private val binder = LocalBinder()

    private val server = Server(object : ServerCallbacks {
        override fun onOpen(server: Server) {
            println("Server opening")
        }

        override fun onPairingRequest(clientName: String, clientGuid: String): String? {
            return null
        }

        override fun onConnectionRequest(clientGuid: String): String? {
            return null
        }

        override fun onPlayRequest(connectionString: String) {
            clientRequestedPlayback = ClientRequestedPlayback(
                connectionString,
                0,
                false
            )
            clientRequestedPlaybackMLD.postValue(clientRequestedPlayback)
        }

        override fun onPauseRequest() {
            if (clientRequestedPlayback != null) {
                clientRequestedPlayback = clientRequestedPlayback?.copy(paused = true)
                clientRequestedPlaybackMLD.postValue(clientRequestedPlayback)
            }
        }

        override fun onResumeRequest() {
            if (clientRequestedPlayback != null) {
                clientRequestedPlayback = clientRequestedPlayback?.copy(paused = false)
                clientRequestedPlaybackMLD.postValue(clientRequestedPlayback)
            }
        }

        override fun onStopRequest() {
            clientRequestedPlaybackMLD.postValue(null)
        }

        override fun onSeekRequest(newTimeElapsed: Long) {
            if (clientRequestedPlayback != null) {
                clientRequestedPlayback = clientRequestedPlayback?.copy(timeElapsed = newTimeElapsed)
                clientRequestedPlaybackMLD.postValue(clientRequestedPlayback)
            }
        }

        override fun onClose(error: Throwable?) {
            println("Server closing")
        }
    })

    override fun onBind(intent: Intent?): IBinder {
        server.open()
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        server.close()
        return false
    }
}