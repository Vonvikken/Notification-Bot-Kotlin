package org.vonvikken.notificationbot

internal class Message private constructor(title: String, text: () -> String, emoji: String? = null) {

    enum class Type(val title: String, val emoji: String? = null) {
        APPLICATION("Notification Bot", "bell"),
        SERVICE("Service message", "gear"),
        NOTIFICATION("Notification received", "incoming_envelope"),
        HELP("Available commands", "information_source")
    }

    companion object {
        internal fun createMessage(messageType: Type, text: () -> String): Message =
            Message(messageType.title, text, messageType.emoji)
    }

    val formattedText = """${emoji?.emoji() ?: ""} ${title.escape().bold()}
                          |
                          |${text.invoke()}""".trimMargin()
}
