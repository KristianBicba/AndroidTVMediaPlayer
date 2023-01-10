package tpo.mediaplayer.app_tv

fun hexDecode(string: String): ByteArray? {
    if (string.length % 2 == 1) return null
    return string
        .chunked(2)
        .map { it.toInt(16).toByte() }
        .toByteArray()
}

fun hexEncode(bytes: ByteArray): String = bytes.joinToString("") { "%02x".format(it) }