package space.be1ski.vibits.shared.feature.mode.data

import kotlinx.browser.localStorage
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode

private const val KEY_APP_MODE = "vibits_app_mode"

/**
 * Web implementation storing app mode in localStorage.
 */
actual class AppModeStore {
  actual fun load(): LocalAppMode {
    val modeName = localStorage.getItem(KEY_APP_MODE)
    val mode = modeName?.let { runCatching { AppMode.valueOf(it) }.getOrNull() } ?: AppMode.NotSelected
    return LocalAppMode(mode = mode)
  }

  actual fun save(mode: LocalAppMode) {
    localStorage.setItem(KEY_APP_MODE, mode.mode.name)
  }
}
