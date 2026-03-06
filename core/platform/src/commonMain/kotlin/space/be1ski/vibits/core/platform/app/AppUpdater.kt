package space.be1ski.vibits.core.platform.app

expect class AppUpdater() {
  suspend fun upgrade(): Boolean

  fun lastUpgradeLogs(): List<String>

  fun restart()
}
