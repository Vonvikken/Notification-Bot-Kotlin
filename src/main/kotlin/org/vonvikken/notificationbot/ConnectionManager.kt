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

internal class ConnectionManager(config: Config) {

    private val logger by getLogger { }
    private val socketFile: File = Paths.get(config.socketPath).toFile()
    private lateinit var serverThread: Thread
    private lateinit var serverSocket: AFUNIXServerSocket

    internal var onReceivedCallback: OptionalConsumer<String> = null
    internal var serviceMessageCallback: OptionalConsumer<String> = null

    init {
        socketFile.deleteOnExit()
    }

    companion object {
        val isRunning = AtomicBoolean()
    }

    internal fun serverStart() {
        if (!isRunning.getAndSet(true)) {
            logger.info("Socket server start requested.")
            serverThread = thread(start = true, name = "Socket server") {
                serverSocket = AFUNIXServerSocket.newInstance().also {
                    it.use { server ->
                        server.bind(AFUNIXSocketAddress.of(socketFile))

                        serviceMessageCallback?.invoke("Socket server started")
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
                                serviceMessageCallback?.invoke("Socket server stopped")
                                socketFile.delete()
                                break
                            }
                        }
                    }
                }
            }
        } else {
            serviceMessageCallback?.invoke("Socket server already started")
        }
    }

    internal fun serverStop() {
        if (::serverThread.isInitialized) {
            logger.info("Socket server termination requested.")
            serverThread.interrupt()
            serverSocket.close()
            isRunning.set(false)
        }
    }

    internal fun serverInfo() {
        serviceMessageCallback?.invoke("Socket server is ${if (isRunning.get()) "" else "in"}active")
    }
}
