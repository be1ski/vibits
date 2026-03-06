package space.be1ski.vibits.core.platform.app

actual class AppUpdater {
  actual suspend fun upgrade(): Boolean = false

  actual fun lastUpgradeLogs(): List<String> = emptyList()

  actual fun restart() = Unit
}
