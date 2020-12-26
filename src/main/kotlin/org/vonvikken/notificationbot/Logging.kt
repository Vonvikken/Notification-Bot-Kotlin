package org.vonvikken.notificationbot

import org.slf4j.Logger
import org.slf4j.LoggerFactory

internal fun getLogger(instance: () -> Unit): Lazy<Logger> = lazy { LoggerFactory.getLogger(getClassName(instance.javaClass)) }

private fun <T : Any> getClassName(clazz: Class<T>): String = clazz.name.replace(Regex("""\$.*$"""), "")
