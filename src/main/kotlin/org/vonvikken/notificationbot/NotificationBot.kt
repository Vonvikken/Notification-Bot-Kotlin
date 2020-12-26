package org.vonvikken.notificationbot

import com.beust.klaxon.Json
import com.beust.klaxon.Klaxon
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
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

    companion object {
        val escapedCharacters =
            arrayOf('_', '*', '[', ']', '(', ')', '~', '`', '>', '#', '+', '-', '=', '|', '{', '}', '.', '!')
    }

    init {
        val (myToken, chatId) = parseCredentials(Paths.get(credentialsFilePath).toFile())
        chatID = chatId

        bot = bot {
            token = myToken
            logLevel = LogLevel.Error
            dispatch {
                command("start") {
                    if (checkMessageChatId(update.message)) {
                        val text = update.message?.text ?: ""
                        sendMessage(text)
                    }
                }
            }
        }
        bot.startPolling()
        sendMessage("_*${"Notification bot started!".escape()}*_")
        logger.info("Notification bot started")
    }

    private fun sendMessage(text: String, escapeText: Boolean = false) {
        val result = bot.sendMessage(chatID, if (escapeText) text.escape() else text, ParseMode.MARKDOWN_V2)
        logger.debug("Sent message $text")
        result.fold(
            {
                logger.debug(it?.result?.text ?: "[Empty response]")
            },
            {
                logger.error(it.errorBody?.toString() ?: "[Empty error]")
            }
        )
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

private fun String.escape(): String = map {
    if (it in NotificationBot.escapedCharacters) {
        """\$it"""
    } else {
        it
    }
}.joinToString(separator = "")

private fun parseCredentials(path: File): ConnectionCredentials =
    Klaxon().parse<ConnectionCredentials>(path) ?: throw IOException("Cannot parse config file!")

internal data class ConnectionCredentials(val token: String, @Json(name = "chat_id") val chatID: Long)
