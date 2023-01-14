package tpo.mediaplayer.lib_communications.server

import kotlinx.coroutines.runBlocking
import kotlin.test.Ignore
import kotlin.test.Test

internal class ServerUtilsKtTest {
    @Test
    @Ignore
    fun exampleGetNetworkAddresses(): Unit = runBlocking {
        getNetworkAddresses()?.forEach { addr ->
            println("$addr, ${addr.address.joinToString(" ") { "%02X".format(it) }}")
        }
    }
}