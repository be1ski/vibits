package space.be1ski.vibits.shared.feature.settings.data

import space.be1ski.vibits.shared.data.local.DesktopStoragePaths
import java.util.prefs.Preferences

/**
 * Desktop implementation backed by Preferences API.
 */
actual class PreferencesStore {
  private val prefs = Preferences.userRoot().node(DesktopStoragePaths.preferencesNode())

  actual fun load(): LocalUserPreferences {
    val defaultTab = LocalUserPreferences.DEFAULT_TIME_RANGE_TAB
    val defaultLanguage = LocalUserPreferences.DEFAULT_LANGUAGE
    val defaultTheme = LocalUserPreferences.DEFAULT_THEME
    val legacyTab = prefs.get("ui_time_range_tab", null)
    val habitsTab = prefs.get("ui_habits_time_range_tab", legacyTab ?: defaultTab)
    val postsTab = prefs.get("ui_posts_time_range_tab", legacyTab ?: defaultTab)
    val language = prefs.get("ui_language", defaultLanguage)
    val theme = prefs.get("ui_theme", defaultTheme)
    return LocalUserPreferences(
      habitsTimeRangeTab = habitsTab,
      postsTimeRangeTab = postsTab,
      language = language,
      theme = theme,
    )
  }

  actual fun save(preferences: LocalUserPreferences) {
    prefs.put("ui_habits_time_range_tab", preferences.habitsTimeRangeTab)
    prefs.put("ui_posts_time_range_tab", preferences.postsTimeRangeTab)
    prefs.put("ui_language", preferences.language)
    prefs.put("ui_theme", preferences.theme)
  }
}
