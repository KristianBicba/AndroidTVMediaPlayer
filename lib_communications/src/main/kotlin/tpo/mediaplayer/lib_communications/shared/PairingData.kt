package tpo.mediaplayer.lib_communications.shared

import java.io.ByteArrayOutputStream
import java.net.InetAddress

data class PairingData(
    val pairingCode: ByteArray,
    val addrs: List<InetAddress>,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PairingData

        if (!pairingCode.contentEquals(other.pairingCode)) return false
        if (addrs != other.addrs) return false

        return true
    }

    override fun hashCode(): Int {
        var result = pairingCode.contentHashCode()
        result = 31 * result + addrs.hashCode()
        return result
    }

    fun toByteArray(): ByteArray {
        val baos = ByteArrayOutputStream()
        baos.writeLengthValue(pairingCode)
        for (addr in addrs) {
            baos.writeLengthValue(addr.address!!)
        }
        return baos.toByteArray()
    }

    companion object {
        @JvmStatic
        fun fromByteArray(byteArray: ByteArray): PairingData? {
            val (rangePairingCode, pairingCode) = byteArray.readLengthValue(0) ?: return null
            val (rangeAddrs, addrs) = byteArray.readInetAddressList(rangePairingCode.last + 1) ?: return null
            if (rangeAddrs.last + 1 != byteArray.size) return null
            return PairingData(pairingCode, addrs)
        }
    }
}
