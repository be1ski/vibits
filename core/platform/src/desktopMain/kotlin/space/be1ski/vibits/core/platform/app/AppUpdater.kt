package space.be1ski.vibits.core.platform.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Files
import java.nio.file.Path
import kotlin.system.exitProcess

private val BREW_PATHS = listOf("/opt/homebrew/bin/brew", "/usr/local/bin/brew")

private fun findBrew(): String? = BREW_PATHS.firstOrNull { Files.exists(Path.of(it)) }

actual class AppUpdater {
  actual suspend fun upgrade(): Boolean =
    withContext(Dispatchers.IO) {
      try {
        val brew = findBrew() ?: return@withContext false
        val update =
          ProcessBuilder(brew, "update")
            .redirectErrorStream(true)
            .start()
        update.waitFor()
        val upgrade =
          ProcessBuilder(brew, "upgrade", "--cask", "vibits")
            .redirectErrorStream(true)
            .start()
        upgrade.waitFor() == 0
      } catch (_: Exception) {
        false
      }
    }

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
