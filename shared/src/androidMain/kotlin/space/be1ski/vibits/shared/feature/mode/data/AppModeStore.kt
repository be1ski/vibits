package space.be1ski.vibits.shared.feature.mode.data

import android.content.Context
import androidx.core.content.edit
import space.be1ski.vibits.shared.data.local.AndroidContextHolder
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode

/**
 * Android implementation backed by SharedPreferences.
 */
actual class AppModeStore {
  private val prefsName = "memos_app_mode"
  private val keyMode = "app_mode"

  actual fun load(): LocalAppMode {
    if (!AndroidContextHolder.isReady()) {
      return LocalAppMode(mode = AppMode.NotSelected)
    }
    val prefs = AndroidContextHolder.context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
    val modeName = prefs.getString(keyMode, null)
    val mode = modeName?.let { runCatching { AppMode.valueOf(it) }.getOrNull() } ?: AppMode.NotSelected
    return LocalAppMode(mode = mode)
  }

  actual fun save(mode: LocalAppMode) {
    if (!AndroidContextHolder.isReady()) {
      return
    }
    val prefs = AndroidContextHolder.context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
    prefs.edit {
      putString(keyMode, mode.mode.name)
    }
  }
}
