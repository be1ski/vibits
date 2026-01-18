package space.be1ski.vibits.shared.core.logging

internal actual fun platformLog(
  level: LogLevel,
  tag: String,
  message: String,
) {
  println("${level.name.first()} | $tag | $message")
}
