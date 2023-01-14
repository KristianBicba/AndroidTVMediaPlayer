package tpo.mediaplayer.app_phone.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import tpo.mediaplayer.app_phone.hexDecode
import java.io.IOException
import java.net.InetAddress

@Entity
data class Device(
    @PrimaryKey(autoGenerate = true) val uid: Int,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "address") val address: String
)

val Device.inetAddress: InetAddress?
    get() = run {
        val hexEncodedInetAddress = address
        val bytestreamInetAddress = hexDecode(hexEncodedInetAddress) ?: return@run null
        val actualInetAddress = try {
            InetAddress.getByAddress(bytestreamInetAddress)
        } catch (_: IOException) {
            return@run null
        }
        actualInetAddress
    }