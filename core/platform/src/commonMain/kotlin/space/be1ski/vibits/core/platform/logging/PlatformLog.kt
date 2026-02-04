package space.be1ski.vibits.core.platform.logging

/**
 * Platform-specific log output.
 * Uses SLF4J on JVM platforms, println on others.
 */
expect fun platformLog(
  level: LogLevel,
  tag: String,
  message: String,
)
