@file:JvmName("NotificationBotMain")

package org.vonvikken.notificationbot

import com.beust.klaxon.Json
import com.beust.klaxon.Klaxon
import java.io.File
import java.io.IOException
import java.nio.file.Paths

const val CONFIG_DEFAULT_PATH = "config.json"
const val SOCKET_DEFAULT_PATH = "/var/tmp/notificationbot.sock"

fun main() {
    val config = Config.parseConfig(Paths.get(CONFIG_DEFAULT_PATH).toFile())

    val connectionManager = ConnectionManager(config)
    val notificationBot = NotificationBot(config)

    notificationBot.startSocketCallback = connectionManager::startServer
    notificationBot.stopSocketCallback = connectionManager::stopServer
    notificationBot.infoSocketCallback = connectionManager::serverInfo

    connectionManager.onReceivedCallback = { notificationBot.sendMessage(it) }
    connectionManager.serviceMessageCallback = { notificationBot.sendMessage("_Service message:_ `${it.escape()}`") }

    connectionManager.startServer()
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
