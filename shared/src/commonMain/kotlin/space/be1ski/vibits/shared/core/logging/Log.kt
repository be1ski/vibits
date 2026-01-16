package space.be1ski.vibits.shared.core.logging

import androidx.compose.runtime.mutableStateListOf
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Simple in-memory log storage for debugging.
 */
object Log {
  private const val MAX_LOGS = 500

  private val _logs = mutableStateListOf<LogEntry>()
  val logs: List<LogEntry> get() = _logs.toList()

  fun d(tag: String, message: String) {
    record(LogLevel.DEBUG, tag, message)
  }

  fun i(tag: String, message: String) {
    record(LogLevel.INFO, tag, message)
  }

  fun w(tag: String, message: String) {
    record(LogLevel.WARN, tag, message)
  }

  fun e(tag: String, message: String, throwable: Throwable? = null) {
    val fullMessage = if (throwable != null) {
      "$message: ${throwable::class.simpleName}: ${throwable.message}"
    } else {
      message
    }
    record(LogLevel.ERROR, tag, fullMessage)
  }

  private fun record(level: LogLevel, tag: String, message: String) {
    val timestamp = Clock.System.now()
      .toLocalDateTime(TimeZone.currentSystemDefault())
    val entry = LogEntry(timestamp.toString(), level, tag, message)

    _logs.add(0, entry)
    while (_logs.size > MAX_LOGS) {
      _logs.removeLastOrNull()
    }

    // Also print to stdout for terminal debugging
    println("${entry.level.name.first()} | $tag | $message")
  }

  fun clear() {
    _logs.clear()
  }

  fun export(): String {
    return _logs.joinToString("\n") { entry ->
      "${entry.timestamp} ${entry.level.name.first()}/$entry.tag: ${entry.message}"
    }
  }
}

data class LogEntry(
  val timestamp: String,
  val level: LogLevel,
  val tag: String,
  val message: String
)

enum class LogLevel {
  DEBUG, INFO, WARN, ERROR
}
