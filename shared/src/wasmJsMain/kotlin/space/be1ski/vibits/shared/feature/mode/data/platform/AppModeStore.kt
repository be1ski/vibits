package space.be1ski.vibits.shared.feature.mode.data.platform

import kotlinx.browser.localStorage
import space.be1ski.vibits.shared.feature.mode.data.LocalAppMode
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode

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
