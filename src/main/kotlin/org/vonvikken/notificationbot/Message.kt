package org.vonvikken.notificationbot

internal class Message private constructor(title: String, textBlock: () -> String, emoji: String? = null) {

    enum class Type(val title: String, val emoji: String? = null) {
        APPLICATION("Notification Bot", "bell"),
        SERVICE("Service message", "gear"),
        NOTIFICATION("Notification received", "incoming_envelope"),
        HELP("Available commands", "information_source")
    }

    companion object {
        internal fun createMessage(messageType: Type, textBlock: () -> String): Message =
            Message(messageType.title, textBlock, messageType.emoji)
    }

    val text = """${emoji?.emoji() ?: ""} ${title.bold()}
                 |
                 |${textBlock.invoke()}""".trimMargin()
}
