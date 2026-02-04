package space.be1ski.vibits.core.platform.logging

import space.be1ski.vibits.core.platform.logging.LogLevel

actual fun platformLog(
  level: LogLevel,
  tag: String,
  message: String,
) {
  println("${level.name.first()} | $tag | $message")
}
