@file:JvmName("NotificationBotMain")

package org.vonvikken.notificationbot

import java.nio.file.Paths

const val TOKEN_DEFAULT_PATH = "token.json"

fun main() {
    // TODO add command line argument
    NotificationBot(Paths.get(TOKEN_DEFAULT_PATH).toFile())
}
