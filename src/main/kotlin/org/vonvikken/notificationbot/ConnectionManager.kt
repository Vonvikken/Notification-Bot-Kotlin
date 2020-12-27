package org.vonvikken.notificationbot

import org.newsclub.net.unix.AFUNIXServerSocket
import org.newsclub.net.unix.AFUNIXSocketAddress
import java.io.File
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
            serverThread = thread(start = true, name = "Socket server thread") {
                serverSocket = AFUNIXServerSocket.newInstance()
                serverSocket.use { server ->
                    server.bind(AFUNIXSocketAddress(socketFile))
                    logger.debug("UNIX socket bound to ${socketFile.absolutePath}")

                    val permissions = setOf(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE)
                    Files.setPosixFilePermissions(socketFile.toPath(), permissions)

                    // TODO find a way to close the socket
                    while (!serverThread.isInterrupted) {
                        logger.info("Waiting for connection...")
                        server.accept().use { socket ->
                            logger.info("Connected!")
                            socket.inputStream.use { input ->
                                val buffer = ByteArray(BUFFER_SIZE)
                                val numRead = input.read(buffer)
                                val str = String(buffer, 0, numRead)
                                logger.debug("Received from socket: $str")
                                onReceivedCallback?.invoke(str)
                            }
                        }
                    }
                }
            }
        }

        logger.info("Server stopped!")
    }

    internal fun stopServer() {
        if (::serverThread.isInitialized) {
            serverThread.interrupt()
            serverSocket.close()
            logger.info("Server termination requested.")
        }
    }
}
