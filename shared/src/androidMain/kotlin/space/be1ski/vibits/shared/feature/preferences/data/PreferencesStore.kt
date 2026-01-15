package space.be1ski.vibits.shared.feature.preferences.data

import android.content.Context
import androidx.core.content.edit
import space.be1ski.vibits.shared.data.local.AndroidContextHolder

/**
 * Android implementation backed by SharedPreferences.
 */
actual class PreferencesStore {
  private val prefsName = "memos_prefs"

  actual fun load(): LocalUserPreferences {
    val defaultTab = LocalUserPreferences.DEFAULT_TIME_RANGE_TAB
    if (!AndroidContextHolder.isReady()) {
      return LocalUserPreferences(habitsTimeRangeTab = defaultTab, postsTimeRangeTab = defaultTab)
    }
    val prefs = AndroidContextHolder.context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
    val legacyTab = prefs.getString("ui_time_range_tab", null)
    val habitsTab = prefs.getString("ui_habits_time_range_tab", legacyTab ?: defaultTab) ?: defaultTab
    val postsTab = prefs.getString("ui_posts_time_range_tab", legacyTab ?: defaultTab) ?: defaultTab
    return LocalUserPreferences(habitsTimeRangeTab = habitsTab, postsTimeRangeTab = postsTab)
  }

  actual fun save(preferences: LocalUserPreferences) {
    if (!AndroidContextHolder.isReady()) {
      return
    }
    val prefs = AndroidContextHolder.context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
    prefs.edit {
      putString("ui_habits_time_range_tab", preferences.habitsTimeRangeTab)
      putString("ui_posts_time_range_tab", preferences.postsTimeRangeTab)
    }
  }
}
