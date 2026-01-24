package space.be1ski.vibits.shared.core.platform.logging

import org.slf4j.LoggerFactory
import space.be1ski.vibits.shared.core.logging.LogLevel

private val logger = LoggerFactory.getLogger("Vibits")

internal actual fun platformLog(
  level: LogLevel,
  tag: String,
  message: String,
) {
  val formattedMessage = "[$tag] $message"
  when (level) {
    LogLevel.DEBUG -> logger.debug(formattedMessage)
    LogLevel.INFO -> logger.info(formattedMessage)
    LogLevel.WARN -> logger.warn(formattedMessage)
    LogLevel.ERROR -> logger.error(formattedMessage)
  }
}
