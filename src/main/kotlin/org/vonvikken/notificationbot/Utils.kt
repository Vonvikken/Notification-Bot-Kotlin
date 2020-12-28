package org.vonvikken.notificationbot

internal typealias OptionalCallback = (() -> Unit)?
internal typealias OptionalConsumer<T> = ((T) -> Unit)?

private val escapedCharacters =
    arrayOf('_', '*', '[', ']', '(', ')', '~', '`', '>', '#', '+', '-', '=', '|', '{', '}', '.', '!')

internal fun String.escape(): String =
    map { if (it in escapedCharacters) """\$it""" else it }.joinToString(separator = "")
