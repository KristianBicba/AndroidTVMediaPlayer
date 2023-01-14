package tpo.mediaplayer.lib_communications.server

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import tpo.mediaplayer.lib_communications.shared.Constants
import tpo.mediaplayer.lib_communications.shared.LineChannelIO
import java.net.ServerSocket
import java.net.Socket
import kotlin.test.Ignore
import kotlin.test.Test

internal class CoroutineSocketServerTest {
    @Test
    @Ignore
    fun exampleBasicServer() {
        class EchoServerImpl(
            val semOpen: Semaphore = Semaphore(1, 1),
            val semClose: Semaphore = Semaphore(1, 1)
        ) : CoroutineSocketServer<EchoServerImpl.Client>(ServerSocket(Constants.PORT)) {
            inner class Client(socket: Socket) : BaseClient(socket) {
                override suspend fun onLine(line: String) {
                    println("onLine($line)")
                    serverLock.withLock {
                        clients.forEach { client ->
                            if (client != this) client.send(line)
                        }
                    }
                }

                override suspend fun onDisconnect(clientDisconnected: Boolean) {
                    println("onDisconnect($clientDisconnected)")
                }
            }

            override suspend fun onOpen() {
                println("onOpen")
                semOpen.release()
            }

            override suspend fun onConnect(socket: Socket): Client {
                println("onConnect($socket)")
                return Client(socket)
            }

            override suspend fun onClose(error: Throwable?) {
                println("onClose($error)")
                semClose.release()
            }

            override suspend fun closeSelf(error: Throwable?) {
                println("closeSelf($error)")
                closeSuspending(error)
            }
        }

        class SimpleClient(private val name: String) {
            private val incoming = Channel<String>(Channel.UNLIMITED)
            private val outgoing = Channel<String>(Channel.UNLIMITED)
            private val socket = Socket("127.0.0.1", Constants.PORT)
            private val client = LineChannelIO(
                socket.getInputStream(), socket.getOutputStream(),
                incoming, outgoing
            )

            suspend fun collectLines(block: suspend (String) -> Unit) {
                while (true) {
                    val line = incoming.receiveCatching().getOrNull() ?: break
                    block(line)
                }
                println("Client $name closed")

                client.close()
                outgoing.close()
                withContext(Dispatchers.IO) {
                    socket.close()
                }
            }

            suspend fun send(line: String) {
                println("Client $name sending $line")
                try {
                    outgoing.send(line)
                } catch (_: ClosedSendChannelException) {
                    println("Client $name failed to send")
                }
            }

            fun close() {
                outgoing.close()
                socket.close()
                client.close()
            }
        }

        runBlocking {
            println("Starting server")
            val server = EchoServerImpl()

            println("Waiting for start")
            server.semOpen.acquire()

            println("Server started, starting clients")
            val client1 = SimpleClient("client1")
            val client2 = SimpleClient("client2")
            val client3 = SimpleClient("client3")

            val job1 = launch { client1.collectLines { println("Client client1 got $it") } }
            val job2 = launch { client2.collectLines { println("Client client2 got $it") } }
            val job3 = launch { client3.collectLines { println("Client client3 got $it") } }

            delay(1000)

            println("Sending a hehe")
            client1.send("hehe")

            delay(1000)

            println("Closing client3")
            withContext(Dispatchers.IO) {
                client3.close()
            }

            delay(1000)

            println("Closing server")
            server.closeSuspending()

            println("Server closed, waiting for clients")
            job1.join()
            job2.join()
            job3.join()
            println("Clients closed, waiting for semaphore")
            server.semClose.acquire()
        }

        println("Done")
    }
}