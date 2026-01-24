package space.be1ski.vibits.shared.core.platform.logging

import space.be1ski.vibits.shared.core.logging.LogLevel

/**
 * Platform-specific log output.
 * Uses SLF4J on JVM platforms, println on others.
 */
internal expect fun platformLog(
  level: LogLevel,
  tag: String,
  message: String,
)
