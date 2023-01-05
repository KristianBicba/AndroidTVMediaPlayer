package tpo.mediaplayer.lib_communications.shared

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import java.io.Closeable
import java.net.Socket

class LineChannelSocket(
    private val socket: Socket,
    private val incoming: SendChannel<String>,
    outgoing: ReceiveChannel<String>
) : Closeable {
    private var closed = false
    private val scope = CoroutineScope(Dispatchers.Default)
    private val internalIncomingChannel = Channel<String>()
    private val lineChannelIO = LineChannelIO(
        socket.getInputStream(), socket.getOutputStream(),
        internalIncomingChannel, outgoing
    )

    init {
        scope.launch {
            try {
                while (true) {
                    incoming.send(internalIncomingChannel.receive())
                }
            } catch (_: ClosedReceiveChannelException) {
            } finally {
                close()
            }
        }
    }

    @Synchronized
    override fun close() {
        if (!closed) {
            closed = true
            return
        }
        scope.cancel()
        internalIncomingChannel.close()
        incoming.close()
        lineChannelIO.close()
        socket.close()
    }
}