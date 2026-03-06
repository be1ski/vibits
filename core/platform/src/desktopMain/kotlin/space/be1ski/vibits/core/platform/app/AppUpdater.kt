package space.be1ski.vibits.core.platform.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.system.exitProcess

actual class AppUpdater {
  actual suspend fun upgrade(): Boolean =
    withContext(Dispatchers.IO) {
      try {
        val process =
          ProcessBuilder("brew", "upgrade", "--cask", "vibits")
            .redirectErrorStream(true)
            .start()
        process.waitFor() == 0
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
