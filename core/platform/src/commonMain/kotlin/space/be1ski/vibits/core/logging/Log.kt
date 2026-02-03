package space.be1ski.vibits.core.logging

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import space.be1ski.vibits.core.platform.logging.platformLog
import kotlin.time.Clock

/**
 * Thread-safe in-memory log storage for debugging.
 */
object Log : SynchronizedObject() {
  private const val MAX_LOGS = 500
  private const val TIMESTAMP_EXPORT_LENGTH = 23 // "2026-01-31 18:37:14.379"

  private val _logs = mutableListOf<LogEntry>()
  val logs: List<LogEntry>
    get() = synchronized(this) { _logs.toList() }

  fun d(
    tag: String,
    message: String,
  ) {
    record(LogLevel.DEBUG, tag, message)
  }

  fun i(
    tag: String,
    message: String,
  ) {
    record(LogLevel.INFO, tag, message)
  }

  fun w(
    tag: String,
    message: String,
  ) {
    record(LogLevel.WARN, tag, message)
  }

  fun e(
    tag: String,
    message: String,
    throwable: Throwable? = null,
  ) {
    val fullMessage =
      if (throwable != null) {
        "$message: ${throwable::class.simpleName}: ${throwable.message}"
      } else {
        message
      }
    record(LogLevel.ERROR, tag, fullMessage)
  }

  private fun record(
    level: LogLevel,
    tag: String,
    message: String,
  ) {
    val timestamp =
      Clock.System
        .now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
    val entry = LogEntry(timestamp.toString(), level, tag, message)

    synchronized(this) {
      _logs.add(0, entry)
      while (_logs.size > MAX_LOGS) {
        _logs.removeLastOrNull()
      }
    }

    platformLog(level, tag, message)
  }

  fun clear() {
    synchronized(this) {
      _logs.clear()
    }
  }

  fun export(): String =
    synchronized(this) {
      _logs.asReversed().joinToString("\n") { entry ->
        "${formatTimestamp(entry.timestamp)} ${entry.level.name.first()}/${entry.tag}: ${entry.message}"
      }
    }

  private fun formatTimestamp(timestamp: String): String =
    timestamp
      .take(TIMESTAMP_EXPORT_LENGTH)
      .replace('T', ' ')
}

data class LogEntry(
  val timestamp: String,
  val level: LogLevel,
  val tag: String,
  val message: String,
)

enum class LogLevel {
  DEBUG,
  INFO,
  WARN,
  ERROR,
}
