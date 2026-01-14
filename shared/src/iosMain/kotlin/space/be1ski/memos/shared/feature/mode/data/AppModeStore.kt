package space.be1ski.memos.shared.feature.mode.data

import platform.Foundation.NSUserDefaults
import space.be1ski.memos.shared.feature.mode.domain.model.AppMode

/**
 * iOS implementation backed by NSUserDefaults.
 */
actual class AppModeStore {
  private val defaults = NSUserDefaults.standardUserDefaults
  private val keyMode = "app_mode"

  actual fun load(): LocalAppMode {
    val modeName = defaults.stringForKey(keyMode)
    val mode = modeName?.let { runCatching { AppMode.valueOf(it) }.getOrNull() } ?: AppMode.NotSelected
    return LocalAppMode(mode = mode)
  }

  actual fun save(mode: LocalAppMode) {
    defaults.setObject(mode.mode.name, forKey = keyMode)
  }
}
