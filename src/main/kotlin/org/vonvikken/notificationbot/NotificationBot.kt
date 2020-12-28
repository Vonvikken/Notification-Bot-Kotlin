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

    init {
        val (myToken, chatId) = parseCredentials(Paths.get(credentialsFilePath).toFile())
        chatID = chatId

        bot = bot {
            token = myToken
            logLevel = LogLevel.Error
            dispatch {
                command("startSocket") {
                    execIfAuthorized(startSocketCallback)
                }

                command("stopSocket") {
                    execIfAuthorized(stopSocketCallback)
                }
            }
        }
        bot.startPolling()
        sendMessage("_*${"Notification bot started!".escape()}*_")
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

private fun parseCredentials(path: File): ConnectionCredentials =
    Klaxon().parse<ConnectionCredentials>(path) ?: throw IOException("Cannot parse config file!")

internal data class ConnectionCredentials(val token: String, @Json(name = "chat_id") val chatID: Long)
