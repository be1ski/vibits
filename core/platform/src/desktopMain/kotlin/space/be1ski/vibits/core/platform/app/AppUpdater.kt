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
      val command =
        ProcessHandle
          .current()
          .info()
          .command()
          .orElse(null)
      if (command != null) {
        ProcessBuilder(command).start()
      }
    } finally {
      exitProcess(0)
    }
  }
}
