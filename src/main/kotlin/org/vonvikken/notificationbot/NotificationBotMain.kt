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

fun main(args: Array<String>) {
    val logger by getLogger { }
    var configPath = CONFIG_DEFAULT_PATH
    val config: Config

    if (args.isNotEmpty()) {
        when (args[0]) {
            "-h", "--help" -> {
                print(
                    """Options:
                      |    -h,--help: print this help message.
                      |    -c, --config <CONFIG_PATH>: path to the config file (default: './config.json').
                    """.trimMargin()
                )
                return
            }
            "-c", "--config" -> {
                if (args.size >= 2) {
                    configPath = args[1]
                } else {
                    logger.error("Config file path not specified! Use '-h' for help.")
                    return
                }
            }
            else -> {
                logger.error("Unrecognized option '${args[0]}'! Use '-h' for help.")
                return
            }
        }
    }

    try {
        config = Config.parseConfig(Paths.get(configPath).toFile())
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
                "${"stop_sign".emoji()} ${"Bot stopped!".italic().bold()} ${"hand".emoji()}"
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
