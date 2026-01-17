package space.be1ski.vibits.shared.feature.settings.data

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
    val defaultLanguage = LocalUserPreferences.DEFAULT_LANGUAGE
    val defaultTheme = LocalUserPreferences.DEFAULT_THEME
    if (!AndroidContextHolder.isReady()) {
      return LocalUserPreferences(
        habitsTimeRangeTab = defaultTab,
        postsTimeRangeTab = defaultTab,
        language = defaultLanguage,
        theme = defaultTheme,
      )
    }
    val prefs = AndroidContextHolder.context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
    val legacyTab = prefs.getString("ui_time_range_tab", null)
    val habitsTab = prefs.getString("ui_habits_time_range_tab", legacyTab ?: defaultTab) ?: defaultTab
    val postsTab = prefs.getString("ui_posts_time_range_tab", legacyTab ?: defaultTab) ?: defaultTab
    val language = prefs.getString("ui_language", defaultLanguage) ?: defaultLanguage
    val theme = prefs.getString("ui_theme", defaultTheme) ?: defaultTheme
    return LocalUserPreferences(
      habitsTimeRangeTab = habitsTab,
      postsTimeRangeTab = postsTab,
      language = language,
      theme = theme,
    )
  }

  actual fun save(preferences: LocalUserPreferences) {
    if (!AndroidContextHolder.isReady()) {
      return
    }
    val prefs = AndroidContextHolder.context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
    prefs.edit {
      putString("ui_habits_time_range_tab", preferences.habitsTimeRangeTab)
      putString("ui_posts_time_range_tab", preferences.postsTimeRangeTab)
      putString("ui_language", preferences.language)
      putString("ui_theme", preferences.theme)
    }
  }
}
