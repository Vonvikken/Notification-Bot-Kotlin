package org.vonvikken.notificationbot

import org.newsclub.net.unix.AFUNIXServerSocket
import org.newsclub.net.unix.AFUNIXSocketAddress
import java.io.File
import java.net.SocketException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.attribute.PosixFilePermission
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

const val BUFFER_SIZE = 4096

internal class ConnectionManager(socketPath: String) {

    private val logger by getLogger { }
    private val socketFile: File = Paths.get(socketPath).toFile()
    private lateinit var serverThread: Thread
    private lateinit var serverSocket: AFUNIXServerSocket

    internal var onReceivedCallback: OptionalConsumer<String> = null

    init {
        socketFile.deleteOnExit()
    }

    companion object {
        val isRunning = AtomicBoolean()
    }

    internal fun startServer() {
        if (!isRunning.getAndSet(true)) {
            serverThread = thread(start = true, name = "Socket server") {
                serverSocket = AFUNIXServerSocket.newInstance()
                serverSocket.use { server ->
                    server.bind(AFUNIXSocketAddress(socketFile))

                    // TODO separate callback for service messages
                    onReceivedCallback?.invoke("_Socket server started\\!_")
                    logger.debug("UNIX socket bound to ${socketFile.absolutePath}")

                    val permissions = setOf(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE)
                    Files.setPosixFilePermissions(socketFile.toPath(), permissions)

                    while (!serverThread.isInterrupted) {
                        logger.debug("Waiting for connection...")
                        try {
                            server.accept().use { socket ->
                                logger.debug("Connected!")
                                socket.inputStream.use { input ->
                                    val buffer = ByteArray(BUFFER_SIZE)
                                    val numRead = input.read(buffer)
                                    val str = String(buffer, 0, numRead)
                                    logger.debug("Received from socket: $str")
                                    onReceivedCallback?.invoke(str)
                                }
                            }
                        } catch (exc: SocketException) {
                            logger.debug("Socket closed!")
                            // TODO separate callback for service messages
                            onReceivedCallback?.invoke("Socket server stopped\\!")
                            socketFile.delete()
                            break
                        }
                    }
                }
            }
        } else {
            // TODO separate callback for service messages
            onReceivedCallback?.invoke("Socket server already started\\!")
        }
    }

    internal fun stopServer() {
        if (::serverThread.isInitialized) {
            logger.info("Socket server termination requested.")
            serverThread.interrupt()
            serverSocket.close()
            isRunning.set(false)
        }
    }
}
