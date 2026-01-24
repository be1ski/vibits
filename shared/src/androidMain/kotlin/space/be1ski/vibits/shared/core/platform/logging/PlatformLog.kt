package space.be1ski.vibits.shared.core.platform.logging

import space.be1ski.vibits.shared.core.logging.LogLevel

internal actual fun platformLog(
  level: LogLevel,
  tag: String,
  message: String,
) {
  when (level) {
    LogLevel.DEBUG -> android.util.Log.d(tag, message)
    LogLevel.INFO -> android.util.Log.i(tag, message)
    LogLevel.WARN -> android.util.Log.w(tag, message)
    LogLevel.ERROR -> android.util.Log.e(tag, message)
  }
}
