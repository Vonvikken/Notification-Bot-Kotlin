package org.vonvikken.notificationbot

import com.beust.klaxon.Json
import com.beust.klaxon.Klaxon
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.logging.LogLevel
import com.github.kotlintelegrambot.network.fold
import java.io.File
import java.io.IOException

class NotificationBot(credentialsFile: File) {
    private val logger by getLogger { }
    private val bot: Bot

    init {
        val (myToken, chatID) = parseCredentials(credentialsFile)
        logger.debug("Token: $myToken")
        logger.debug("Chat ID: $chatID")
        bot = bot {
            token = myToken
            logLevel = LogLevel.All()
            dispatch {
                command("start") {
                    val text = update.message?.text ?: "Empty message!"
                    val result = bot.sendMessage(chatID, text)
                    logger.info("Received $text")
                    result.fold(
                        {
                            logger.info(it?.result?.text ?: "Empty response")
                        },
                        {
                            logger.error(it.errorBody?.toString() ?: "Empty error")
                        }
                    )
                }
            }
        }
        bot.startPolling()
    }
}

private fun parseCredentials(path: File): ConnectionCredentials =
    Klaxon().parse<ConnectionCredentials>(path) ?: throw IOException("Cannot parse config file!")

internal data class ConnectionCredentials(val token: String, @Json(name = "chat_id") val chatID: Long)
