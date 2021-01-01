@file:Suppress("unused")

package org.vonvikken.notificationbot

import com.vdurmont.emoji.EmojiManager
import com.vdurmont.emoji.EmojiParser

internal typealias OptionalCallback = (() -> Unit)?
internal typealias OptionalConsumer<T> = ((T) -> Unit)?

internal fun String.emoji(): String = EmojiParser.parseToUnicode(":$this:").let {
    if (EmojiManager.isEmoji(it)) it else ""
}

internal fun String.bold(): String = "<b>$this</b>"
internal fun String.italic(): String = "<i>$this</i>"
internal fun String.monospace(): String = "<code>$this</code>"
internal fun String.strikethrough(): String = "<s>$this</s>"
internal fun String.underline(): String = "<u>$this</u>"
