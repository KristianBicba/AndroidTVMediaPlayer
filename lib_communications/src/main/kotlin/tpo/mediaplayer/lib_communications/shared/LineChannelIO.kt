package tpo.mediaplayer.lib_communications.shared

import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.runBlocking
import java.io.IOException
import java.io.InputStream
import java.io.InterruptedIOException
import java.io.OutputStream
import kotlin.concurrent.thread

class LineChannelIO(
    private val inputStream: InputStream,
    private val outputStream: OutputStream,
    private val incoming: SendChannel<String>,
    private val outgoing: ReceiveChannel<String>
) : AutoCloseable {
    private var closed = false

    private val threadRx = thread {
        try {
            inputStream.bufferedReader().use { reader ->
                while (true) {
                    val line = try {
                        reader.readLine() ?: break
                    } catch (_: IOException) {
                        break
                    }
                    val result = incoming.trySendBlocking(line)
                    if (!result.isSuccess) break
                }
            }
        } catch (_: InterruptedException) {
        } catch (_: InterruptedIOException) {
        } finally {
            close()
        }
    }

    private val threadTx = thread {
        try {
            outputStream.writer().use { writer ->
                while (true) {
                    val result = runBlocking { outgoing.receiveCatching() }
                    if (!result.isSuccess) break
                    val toWrite = result.getOrNull() ?: break
                    try {
                        writer.append("$toWrite\n")
                        writer.flush()
                    } catch (_: IOException) {
                        break
                    }
                }
            }
        } catch (_: InterruptedException) {
        } catch (_: InterruptedIOException) {
        } finally {
            close()
        }
    }

    @Synchronized
    override fun close() {
        if (closed) {
            closed = true
            return
        }
        threadRx.interrupt()
        threadTx.interrupt()
        inputStream.close()
        outputStream.close()
        incoming.close()
    }
}