package tpo.mediaplayer.lib_communications.client

import tpo.mediaplayer.lib_communications.shared.PairingData
import tpo.mediaplayer.lib_communications.shared.PlaybackStatus
import java.io.IOException
import java.net.InetAddress

class ClientPairingHelper private constructor(val address: InetAddress, val name: String) {
    companion object {
        fun attemptToPair(pairingData: PairingData, myName: String, myGuid: String): ClientPairingHelper? {
            for (addr in pairingData.addrs) {
                lateinit var client: Client
                try {
                    client = Client(object : ClientCallbacks {
                        override fun onUpdateNowPlaying(newValue: PlaybackStatus) {}
                        override fun onClose(error: Throwable?) {}
                    }, addr)
                    val pairingResult = client.pair(pairingData.pairingCode, myName, myGuid)
                    if (client.isEstablished && pairingResult is ClientPairingResult.Success) {
                        return ClientPairingHelper(addr, pairingResult.tvName)
                    }
                } catch (_: IOException) {
                } finally {
                    client.close()
                }
            }
            return null
        }
    }
}
