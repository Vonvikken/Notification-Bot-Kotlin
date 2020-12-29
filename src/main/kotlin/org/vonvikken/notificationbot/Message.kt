package org.vonvikken.notificationbot

internal sealed class Message(title: String, text: () -> String, emoji: String? = null) {

    enum class Type {
        APPLICATION, SERVICE, NOTIFICATION, HELP
    }

    companion object {
        internal fun createMessage(messageType: Type, text: () -> String): Message = when (messageType) {
            Type.APPLICATION -> ApplicationMessage(text)
            Type.SERVICE -> ServiceMessage(text)
            Type.NOTIFICATION -> NotificationMessage(text)
            Type.HELP -> HelpMessage(text)
        }
    }

    val formattedText = """${emoji?.emoji() ?: ""} ${title.escape().bold()}
                          |
                          |${text.invoke()}""".trimMargin()
}

private class ApplicationMessage(text: () -> String) : Message("Notification Bot", text, "bell")

private class ServiceMessage(text: () -> String) : Message("Service message", text, "gear")

private class NotificationMessage(text: () -> String) : Message("Notification received", text, "incoming_envelope")

private class HelpMessage(text: () -> String) : Message("Available commands", text, "information_source")
