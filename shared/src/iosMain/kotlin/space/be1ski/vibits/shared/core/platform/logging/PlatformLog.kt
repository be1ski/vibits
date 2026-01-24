package space.be1ski.vibits.shared.core.platform.logging

import space.be1ski.vibits.shared.core.logging.LogLevel

internal actual fun platformLog(
  level: LogLevel,
  tag: String,
  message: String,
) {
  println("${level.name.first()} | $tag | $message")
}
