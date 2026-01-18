package space.be1ski.vibits.shared.feature.mode.data.platform

import platform.Foundation.NSUserDefaults
import space.be1ski.vibits.shared.feature.mode.data.LocalAppMode
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode

actual class AppModeStore {
  private val defaults = NSUserDefaults.standardUserDefaults
  private val keyMode = "app_mode"

  actual fun load(): LocalAppMode {
    val modeName = defaults.stringForKey(keyMode)
    val mode = modeName?.let { runCatching { AppMode.valueOf(it) }.getOrNull() } ?: AppMode.NOT_SELECTED
    return LocalAppMode(mode = mode)
  }

  actual fun save(mode: LocalAppMode) {
    defaults.setObject(mode.mode.name, forKey = keyMode)
  }
}
