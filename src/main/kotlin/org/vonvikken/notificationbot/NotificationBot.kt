package org.vonvikken.notificationbot

import com.beust.klaxon.Json
import com.beust.klaxon.Klaxon
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.handlers.CommandHandlerEnvironment
import com.github.kotlintelegrambot.entities.Message
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.logging.LogLevel
import com.github.kotlintelegrambot.network.fold
import java.io.File
import java.io.IOException
import java.nio.file.Paths

class NotificationBot(credentialsFilePath: String) {
    private val logger by getLogger { }
    private val bot: Bot
    private val chatID: Long

    var startSocketCallback: OptionalCallback = null
    var stopSocketCallback: OptionalCallback = null
    var infoSocketCallback: OptionalCallback = null

    init {
        val (myToken, chatId) = parseCredentials(Paths.get(credentialsFilePath).toFile())
        chatID = chatId

        bot = bot {
            token = myToken
            logLevel = LogLevel.Error
            dispatch {
                command(Command.SERVICE_START.commandName) {
                    execIfAuthorized(startSocketCallback)
                }

                command(Command.SERVICE_STOP.commandName) {
                    execIfAuthorized(stopSocketCallback)
                }

                command(Command.SERVICE_INFO.commandName) {
                    execIfAuthorized(infoSocketCallback)
                }

                command(Command.HELP.commandName) {
                    execIfAuthorized {
                        val stringBuilder = StringBuilder()
                        stringBuilder.appendLine("*Available commands:*").appendLine()
                        Command.values().forEach { cmd ->
                            stringBuilder.append("\u2022 `/${cmd.commandName}` ")
                                .appendLine("\u2192 ${cmd.description.escape()}")
                        }
                        sendMessage("$stringBuilder")
                    }
                }
            }
        }
        bot.startPolling()

        sendMessage(
            """*${"Notification bot started!".escape()}*
               |
               |_Use `/help` to list all the available commands_
            """.trimMargin()
        )
        logger.info("Notification bot started")
    }

    internal fun sendMessage(text: String, escapeText: Boolean = false) {
        val result = bot.sendMessage(chatID, if (escapeText) text.escape() else text, ParseMode.MARKDOWN_V2)
        logger.debug("Sent message: $text")
        result.fold(
            {
                logger.debug("Response: ${it?.result?.text ?: "[Empty response]"}")
            },
            {
                logger.error("Error! ${it.errorBody?.toString() ?: "[Empty error]"}")
            }
        )
    }

    private fun CommandHandlerEnvironment.execIfAuthorized(block: OptionalCallback) {
        if (block != null && checkMessageChatId(update.message)) {
            block.invoke()
        }
    }

    private fun checkMessageChatId(message: Message?): Boolean {
        if (message == null)
            return false

        val username = message.from?.username ?: "[Unknown username]"
        val text = message.text ?: "[No text]"
        val chatId = message.chat.id

        val isOk = chatID == chatId

        if (isOk) {
            logger.info("Message received from user $username:\n\t$text")
        } else {
            logger.warn("Message received from unauthorized user $username:\n\t$text")
        }

        return isOk
    }
}

internal data class ConnectionCredentials(val token: String, @Json(name = "chat_id") val chatID: Long)

private fun parseCredentials(path: File): ConnectionCredentials =
    Klaxon().parse<ConnectionCredentials>(path) ?: throw IOException("Cannot parse config file!")

private enum class Command(val commandName: String, val description: String) {
    SERVICE_START("serviceStart", "Start the notification listener service."),
    SERVICE_STOP("serviceStop", "Stop the notification listener service."),
    SERVICE_INFO("serviceInfo", "Status of the notification listener service."),
    HELP("help", "Print command list."),
}
