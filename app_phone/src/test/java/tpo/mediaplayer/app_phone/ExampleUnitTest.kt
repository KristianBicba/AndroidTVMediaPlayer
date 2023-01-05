package tpo.mediaplayer.app_phone

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Test

import org.junit.Assert.*
import tpo.mediaplayer.lib_communications.shared.LineChannelIO

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testLineChannel() = runBlocking {
        val channelIn = Channel<String>(Channel.UNLIMITED)
        val channelOut = Channel<String>(Channel.UNLIMITED)

        val channelIO = LineChannelIO("asdgdas\nwqwqqdqw".byteInputStream(), System.out, channelIn, channelOut)
        System.err.println("Starting jobs")

        val job1 = launch {
            while (true) {
                System.err.println("Entering loop")
                try {
                    val recv = channelIn.receive()
                    System.err.println(recv)
                } catch (_: ClosedReceiveChannelException) {
                    channelOut.close()
                    break
                }
            }
        }

        val job2 = launch {
            while (true) {
                delay(3000)
                try {
                    channelOut.send("hahaha")
                } catch (_: ClosedSendChannelException) {
                    break
                }
            }
        }

        job1.join()
        System.err.println("Job1 exited")
        job2.join()
        System.err.println("Job2 exited.")
    }
}