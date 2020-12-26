@file:JvmName("NotificationBotMain")

package org.vonvikken.notificationbot

const val TOKEN_DEFAULT_PATH = "token.json"
const val SOCKET_DEFAULT_PATH = "/var/tmp/notificationbot.sock"

fun main() {
    // TODO add command line argument
    NotificationBot(TOKEN_DEFAULT_PATH)
}
