@file:JvmName("NotificationBotMain")

package org.vonvikken.notificationbot

const val TOKEN_DEFAULT_PATH = "token.json"
const val SOCKET_DEFAULT_PATH = "/var/tmp/notificationbot.sock"

fun main() {
    val connectionManager = ConnectionManager(SOCKET_DEFAULT_PATH)
    val notificationBot = NotificationBot(TOKEN_DEFAULT_PATH)

    notificationBot.startSocketCallback = connectionManager::startServer
    notificationBot.stopSocketCallback = connectionManager::stopServer
    connectionManager.onReceivedCallback = { notificationBot.sendMessage(it) }

    connectionManager.startServer()
}
