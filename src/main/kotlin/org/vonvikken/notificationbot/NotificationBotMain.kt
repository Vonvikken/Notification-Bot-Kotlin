@file:JvmName("NotificationBotMain")

package org.vonvikken.notificationbot

import com.beust.klaxon.Json
import com.beust.klaxon.Klaxon
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.file.Paths

const val CONFIG_DEFAULT_PATH = "config.json"
const val SOCKET_DEFAULT_PATH = "/var/tmp/notificationbot.sock"

fun main() {
    val logger by getLogger { }
    val config: Config

    try {
        config = Config.parseConfig(Paths.get(CONFIG_DEFAULT_PATH).toFile())
    } catch (fnfe: FileNotFoundException) {
        logger.error("Unable to find config file!")
        return
    }

    val connectionManager = ConnectionManager(config)
    val notificationBot = NotificationBot(config)

    notificationBot.serverStartCallback = connectionManager::serverStart
    notificationBot.serverStopCallback = connectionManager::serverStop
    notificationBot.serverInfoCallback = connectionManager::serverInfo

    connectionManager.onReceivedCallback = notificationBot::sendNotificationMessage
    connectionManager.serviceMessageCallback = notificationBot::sendServiceMessage

    Runtime.getRuntime().addShutdownHook(object : Thread() {
        override fun run() {
            connectionManager.serverStop()
            notificationBot.sendApplicationMessage {
                "${"stop_sign".emoji()} ${"Bot stopped!".escape().italic().bold()} ${"hand".emoji()}"
            }
            logger.info("Notification bot stopped.")
        }
    })

    connectionManager.serverStart()
}

internal data class Config(
    val token: String,
    @Json(name = "chat_id") val chatID: Long,
    @Json(name = "socket_path") val socketPath: String = SOCKET_DEFAULT_PATH
) {
    companion object {
        internal fun parseConfig(path: File): Config =
            Klaxon().parse<Config>(path) ?: throw IOException("Cannot parse config file!")
    }
}
