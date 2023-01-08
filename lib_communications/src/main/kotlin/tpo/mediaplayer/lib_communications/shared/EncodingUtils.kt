package tpo.mediaplayer.lib_communications.shared

import java.io.ByteArrayOutputStream
import java.net.InetAddress
import java.net.UnknownHostException

fun ByteArray.rangeOrNull(range: IntRange): ByteArray? = try {
    copyOfRange(range.first, range.last + 1)
} catch (_: IndexOutOfBoundsException) {
    null
} catch (_: java.lang.IllegalArgumentException) {
    null
}

fun ByteArray.readLengthValue(pos: Int): Pair<IntRange, ByteArray>? {
    val length = (getOrNull(pos) ?: return null).toUByte().toInt() + 1
    val result = rangeOrNull(pos + 1 until pos + length + 1) ?: return null
    return Pair(pos until pos + length + 1, result)
}

fun ByteArray.readInetAddress(pos: Int): Pair<IntRange, InetAddress>? {
    val (range, bytes) = readLengthValue(pos) ?: return null
    val addr = try {
        InetAddress.getByAddress(bytes)
    } catch (_: UnknownHostException) {
        null
    } ?: return null
    return Pair(range, addr)
}

fun ByteArray.readInetAddressList(pos: Int): Pair<IntRange, List<InetAddress>>? {
    var currentPos = pos
    val addrList = mutableListOf<InetAddress>()
    while (true) {
        val (range, addr) = readInetAddress(currentPos) ?: break
        addrList += addr
        currentPos = range.last + 1
    }
    if (currentPos == pos) return null
    return Pair(pos until currentPos, addrList)
}

fun ByteArrayOutputStream.writeLengthValue(bytes: ByteArray) {
    if (bytes.size > 256) throw java.lang.IllegalArgumentException()
    write(bytes.size - 1)
    write(bytes)
}
