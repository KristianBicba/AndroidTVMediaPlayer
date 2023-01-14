package tpo.mediaplayer.app_phone

import android.content.Context
import android.os.Handler
import android.widget.Toast
import tpo.mediaplayer.app_phone.db.Device
import tpo.mediaplayer.lib_communications.client.ClientPairingHelper.Companion.attemptToPair
import tpo.mediaplayer.lib_communications.shared.PairingData

fun attemptPairing(data: PairingData, myGuid: String, context: Context) {
    Thread {
        val result = attemptToPair(data, GodObject.instance.deviceName, myGuid)
        Handler(context.mainLooper).post {
            if (result == null) {
                Toast.makeText(context, context.getString(R.string.pairing_status_success), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, context.getString(R.string.pairing_status_failure), Toast.LENGTH_SHORT).show()
                GodObject.instance.db.deviceDao().insertDevice(
                    Device(0, result.name, hexEncode(result.address.address))
                )
            }
        }
    }.start()
}

fun pairingDataFromHexString(string: String): PairingData? {
    val decoded = hexDecode(string) ?: return null
    return PairingData.fromByteArray(decoded)
}