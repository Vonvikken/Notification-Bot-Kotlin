package org.vonvikken.notificationbot

import com.vdurmont.emoji.EmojiManager
import com.vdurmont.emoji.EmojiParser

internal typealias OptionalCallback = (() -> Unit)?
internal typealias OptionalConsumer<T> = ((T) -> Unit)?

private val escapedCharacters =
    arrayOf('_', '*', '[', ']', '(', ')', '~', '`', '>', '#', '+', '-', '=', '|', '{', '}', '.', '!')

internal fun String.escape(): String =
    map { if (it in escapedCharacters) """\$it""" else it }.joinToString(separator = "")

internal fun String.emoji(): String = EmojiParser.parseToUnicode(":$this:").let {
    if (EmojiManager.isEmoji(it)) it else ""
}
