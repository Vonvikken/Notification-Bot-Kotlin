package org.vonvikken.notificationbot

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.handlers.CommandHandlerEnvironment
import com.github.kotlintelegrambot.entities.Message
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.logging.LogLevel
import com.github.kotlintelegrambot.network.fold

internal class NotificationBot(config: Config) {
    private val logger by getLogger { }
    private val bot: Bot
    private val chatID: Long = config.chatID

    var serverStartCallback: OptionalCallback = null
    var serverStopCallback: OptionalCallback = null
    var serverInfoCallback: OptionalCallback = null

    init {

        bot = bot {
            token = config.token
            logLevel = LogLevel.Error
            dispatch {
                command(Command.SERVICE_START.commandName) {
                    execIfAuthorized(serverStartCallback)
                }

                command(Command.SERVICE_STOP.commandName) {
                    execIfAuthorized(serverStopCallback)
                }

                command(Command.SERVICE_INFO.commandName) {
                    execIfAuthorized(serverInfoCallback)
                }

                command(Command.HELP.commandName) {
                    execIfAuthorized {
                        val stringBuilder = StringBuilder()
                        stringBuilder.appendLine("${"information_source".emoji()}  *Available commands*").appendLine()
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
            """${"bell".emoji()} *${"Notification bot started!".escape()}*
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

    internal fun sendServiceMessage(text: String) {
        sendMessage(
            """${"gear".emoji()} *Service message*
               |
               |`${text.escape()}`""".trimMargin()
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

private enum class Command(val commandName: String, val description: String) {
    SERVICE_START("serviceStart", "Start the notification listener service."),
    SERVICE_STOP("serviceStop", "Stop the notification listener service."),
    SERVICE_INFO("serviceInfo", "Status of the notification listener service."),
    HELP("help", "Print command list."),
}
