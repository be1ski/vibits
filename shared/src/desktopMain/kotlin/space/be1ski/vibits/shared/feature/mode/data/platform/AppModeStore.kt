package space.be1ski.vibits.shared.feature.mode.data.platform

import space.be1ski.vibits.shared.app.data.DesktopStoragePaths
import space.be1ski.vibits.shared.feature.mode.data.LocalAppMode
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode
import java.util.prefs.Preferences

actual class AppModeStore {
  private val prefs = Preferences.userRoot().node(DesktopStoragePaths.preferencesNode())
  private val keyMode = "app_mode"

  actual fun load(): LocalAppMode {
    val modeName = prefs.get(keyMode, null)
    val mode = modeName?.let { runCatching { AppMode.valueOf(it) }.getOrNull() } ?: AppMode.NOT_SELECTED
    return LocalAppMode(mode = mode)
  }

  actual fun save(mode: LocalAppMode) {
    prefs.put(keyMode, mode.mode.name)
  }
}
