package space.be1ski.vibits.feature.mode.data.platform

import kotlinx.browser.localStorage
import space.be1ski.vibits.core.platform.mode.AppMode
import space.be1ski.vibits.feature.mode.data.LocalAppMode

private const val KEY_APP_MODE = "vibits_app_mode"

actual class AppModeStore {
  actual fun load(): LocalAppMode {
    val modeName = localStorage.getItem(KEY_APP_MODE)
    val mode = modeName?.let { runCatching { AppMode.valueOf(it) }.getOrNull() } ?: AppMode.NOT_SELECTED
    return LocalAppMode(mode = mode)
  }

  actual fun save(mode: LocalAppMode) {
    localStorage.setItem(KEY_APP_MODE, mode.mode.name)
  }
}
