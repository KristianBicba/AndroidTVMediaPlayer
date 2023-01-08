package tpo.mediaplayer.lib_communications.shared

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private data class TestLineChannelIO(
    val obj: LineChannelIO,
    val incomingChannel: ReceiveChannel<String>,
    val outgoingChannel: SendChannel<String>,
    val incomingStream: PipedOutputStream,
    val outgoingStream: ByteArrayOutputStream
) : AutoCloseable {
    companion object {
        fun fromPipe(): TestLineChannelIO {
            val incoming = Channel<String>(Channel.UNLIMITED)
            val outgoing = Channel<String>(Channel.UNLIMITED)

            val incomingStreamWrite = PipedOutputStream()
            val incomingStreamRead = PipedInputStream(incomingStreamWrite)
            val outgoingStream = ByteArrayOutputStream()

            val obj = LineChannelIO(incomingStreamRead, outgoingStream, incoming, outgoing)
            return TestLineChannelIO(obj, incoming, outgoing, incomingStreamWrite, outgoingStream)
        }
    }

    override fun close() {
        obj.close()
        outgoingChannel.close()
        outgoingStream.close()
    }
}

internal class LineChannelIOTest {
    @Test
    fun testLineChannelBasic() = runBlocking {
        val c = TestLineChannelIO.fromPipe()

        withContext(Dispatchers.IO) { c.incomingStream.write("asdf\ndefg\n".toByteArray()) }

        assertEquals("asdf", c.incomingChannel.receive())
        assertEquals("defg", c.incomingChannel.receive())

        c.outgoingChannel.send("hehe")
        delay(100)
        assertEquals("hehe\n", c.outgoingStream.toString())

        c.outgoingChannel.close()
        delay(100)
        assertTrue(c.incomingChannel.tryReceive().isClosed)

        c.close()
    }
}