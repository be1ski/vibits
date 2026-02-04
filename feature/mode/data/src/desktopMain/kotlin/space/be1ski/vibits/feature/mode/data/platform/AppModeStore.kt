package space.be1ski.vibits.feature.mode.data.platform

import space.be1ski.vibits.core.platform.app.DesktopStoragePaths
import space.be1ski.vibits.core.platform.mode.AppMode
import space.be1ski.vibits.feature.mode.data.LocalAppMode
import java.util.prefs.Preferences

actual fun createAppModeStore(): AppModeStore = DesktopAppModeStore()

private class DesktopAppModeStore : AppModeStore {
  private val prefs = Preferences.userRoot().node(DesktopStoragePaths.preferencesNode())
  private val keyMode = "app_mode"

  override fun load(): LocalAppMode {
    val modeName = prefs.get(keyMode, null)
    val mode = modeName?.let { runCatching { AppMode.valueOf(it) }.getOrNull() } ?: AppMode.NOT_SELECTED
    return LocalAppMode(mode = mode)
  }

  override fun save(mode: LocalAppMode) {
    prefs.put(keyMode, mode.mode.name)
  }
}
