package tpo.mediaplayer.lib_communications.server

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException

private val emulatorInetAddress
        by lazy { InetAddress.getByAddress(byteArrayOf(10, 0, 2, 15)) as Inet4Address }

private val emulatorHostInetAddress
        by lazy { InetAddress.getByAddress(byteArrayOf(10, 0, 2, 2)) as Inet4Address }

internal suspend fun getNetworkAddresses(): List<InetAddress>? = coroutineScope {
    val interfaces = withContext(Dispatchers.IO) {
        try {
            NetworkInterface.getNetworkInterfaces()
        } catch (_: SocketException) {
            null
        }
    } ?: return@coroutineScope null

    val ret = mutableListOf<InetAddress>()

    for (iface in interfaces) {
        if (iface == null) continue
        for (addr in iface.interfaceAddresses) {
            val inetAddress = addr?.address ?: continue
            if (inetAddress.isLoopbackAddress || inetAddress.isMulticastAddress) continue
            if (inetAddress == emulatorInetAddress) {
                ret += emulatorHostInetAddress
            } else {
                ret += inetAddress
            }
        }
    }

    ret
}