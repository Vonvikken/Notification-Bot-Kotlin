package org.vonvikken.notificationbot

import com.beust.klaxon.Json
import com.beust.klaxon.Klaxon
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.file.Paths

const val CONFIG_DEFAULT_PATH = "config.json"
const val SOCKET_DEFAULT_PATH = "/var/tmp/notificationbot.sock"

fun main(args: Array<String>) = NotificationBotMain().main(args)

class NotificationBotMain : CliktCommand() {
    private val logger by getLogger { }
    private val configPath by option(
        "-c",
        "--config",
        help = "path to the config file (default: \"./config.json\")"
    ).default(CONFIG_DEFAULT_PATH)

    override fun run() {
        val config: Config = try {
            Config.parseConfig(Paths.get(configPath).toFile())
        } catch (fnfe: FileNotFoundException) {
            logger.error("Unable to find config file!")
            return
        }

        val connectionManager = ConnectionManager(config)
        val notificationBot = NotificationBot(config)

        with(notificationBot) {
            serverStartCallback = connectionManager::serverStart
            serverStopCallback = connectionManager::serverStop
            serverInfoCallback = connectionManager::serverInfo
        }

        with(connectionManager) {
            onReceivedCallback = notificationBot::sendNotificationMessage
            serviceMessageCallback = notificationBot::sendServiceMessage
        }

        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                connectionManager.serverStop()
                notificationBot.sendApplicationMessage {
                    "${"stop_sign".emoji()} ${"Bot stopped!".italic().bold()} ${"hand".emoji()}"
                }
                logger.info("Notification bot stopped.")
            }
        })

        connectionManager.serverStart()
    }
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
