package space.be1ski.memos.shared.data.local

import android.content.Context

/**
 * Android implementation backed by SharedPreferences.
 */
actual class PreferencesStore {
  private val prefsName = "memos_prefs"

  actual fun load(): LocalUserPreferences {
    if (!AndroidContextHolder.isReady()) {
      return LocalUserPreferences(timeRangeTab = LocalUserPreferences.DEFAULT_TIME_RANGE_TAB)
    }
    val prefs = AndroidContextHolder.context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
    val timeRangeTab = prefs.getString("ui_time_range_tab", LocalUserPreferences.DEFAULT_TIME_RANGE_TAB)
      ?: LocalUserPreferences.DEFAULT_TIME_RANGE_TAB
    return LocalUserPreferences(timeRangeTab = timeRangeTab)
  }

  actual fun save(preferences: LocalUserPreferences) {
    if (!AndroidContextHolder.isReady()) {
      return
    }
    val prefs = AndroidContextHolder.context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
    prefs.edit()
      .putString("ui_time_range_tab", preferences.timeRangeTab)
      .apply()
  }
}
