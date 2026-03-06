package space.be1ski.vibits.core.platform.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import space.be1ski.vibits.core.platform.logging.LogLevel
import space.be1ski.vibits.core.platform.logging.platformLog
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.system.exitProcess

private val BREW_PATHS = listOf("/opt/homebrew/bin/brew", "/usr/local/bin/brew")
private const val TAG = "AppUpdater"
private const val MAX_OUTPUT_LINES = 80

private fun brewCandidates(): List<String> {
  val fromEnv =
    System
      .getenv("HOMEBREW_PREFIX")
      ?.trim()
      ?.takeIf { it.isNotBlank() }
      ?.let { "$it/bin/brew" }
  return (listOfNotNull(fromEnv) + BREW_PATHS).distinct()
}

private fun findBrew(): String? = brewCandidates().firstOrNull { Files.isExecutable(Path.of(it)) }

private data class CommandResult(
  val exitCode: Int,
  val output: String,
)

private fun runCommand(vararg args: String): CommandResult {
  val process = ProcessBuilder(*args).redirectErrorStream(true).start()
  val output = process.inputStream.bufferedReader().use { it.readText() }
  return CommandResult(exitCode = process.waitFor(), output = output)
}

private fun log(
  level: LogLevel,
  message: String,
) {
  platformLog(level, TAG, message)
}

private fun logCommandResult(
  command: String,
  result: CommandResult,
  emit: (LogLevel, String) -> Unit,
) {
  val level = if (result.exitCode == 0) LogLevel.INFO else LogLevel.ERROR
  emit(level, "$command exited with code ${result.exitCode}")

  val lines =
    result.output
      .lineSequence()
      .map(String::trimEnd)
      .filter { it.isNotBlank() }
      .toList()

  if (lines.isEmpty()) {
    emit(level, "$command output: <empty>")
    return
  }

  lines.take(MAX_OUTPUT_LINES).forEach { line ->
    emit(level, "$command | $line")
  }
  if (lines.size > MAX_OUTPUT_LINES) {
    emit(level, "$command | ... ${lines.size - MAX_OUTPUT_LINES} more lines truncated")
  }
}

actual class AppUpdater {
  @Volatile
  private var lastLogs: List<String> = emptyList()

  actual suspend fun upgrade(): Boolean =
    withContext(Dispatchers.IO) {
      val collectedLogs = mutableListOf<String>()

      fun emit(
        level: LogLevel,
        message: String,
      ) {
        collectedLogs += "${level.name}: $message"
        log(level, message)
      }

      try {
        val brew = findBrew()
        if (brew == null) {
          emit(LogLevel.ERROR, "brew binary not found. Checked: ${brewCandidates().joinToString()}")
          return@withContext false
        }

        emit(LogLevel.INFO, "Using brew at: $brew")

        val update = runCommand(brew, "update")
        logCommandResult("brew update", update, ::emit)
        if (update.exitCode != 0) {
          emit(LogLevel.ERROR, "Skipping upgrade because brew update failed")
          return@withContext false
        }

        val upgrade = runCommand(brew, "upgrade", "--cask", "vibits")
        logCommandResult("brew upgrade --cask vibits", upgrade, ::emit)
        upgrade.exitCode == 0
      } catch (e: IOException) {
        emit(LogLevel.ERROR, "Upgrade failed with exception: ${e::class.simpleName}: ${e.message}")
        false
      } finally {
        lastLogs = collectedLogs.toList()
      }
    }

  actual fun lastUpgradeLogs(): List<String> = lastLogs

  actual fun restart() {
    try {
      val info = ProcessHandle.current().info()
      val command = info.command().orElse(null) ?: return
      val arguments = info.arguments().orElse(null)
      val fullCommand =
        if (arguments != null) listOf(command) + arguments.toList() else listOf(command)
      ProcessBuilder(fullCommand).start()
    } finally {
      exitProcess(0)
    }
  }
}
