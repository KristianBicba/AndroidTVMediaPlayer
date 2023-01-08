package tpo.mediaplayer.lib_communications.shared

import java.net.InetAddress
import kotlin.test.Test
import kotlin.test.assertEquals

internal class PairingDataTest {
    @Test
    fun testByteArray() {
        val originalData = PairingData(
            "asdf".toByteArray(),
            listOf(
                InetAddress.getByAddress(byteArrayOf(10, 0, 2, 2)),
                InetAddress.getByAddress(byteArrayOf(10, 0, 2, 15)),
                InetAddress.getByAddress(byteArrayOf(192.toByte(), 168.toByte(), 0, 113)),
            )
        )
        println(originalData)
        val encodedData = originalData.toByteArray()
        println(encodedData.joinToString(" ") { "%02X".format(it) })
        val decodedData = PairingData.fromByteArray(encodedData)
        println(decodedData)
        assertEquals(originalData, decodedData)
    }
}