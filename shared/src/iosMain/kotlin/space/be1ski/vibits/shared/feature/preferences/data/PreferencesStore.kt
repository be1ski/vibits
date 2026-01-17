package space.be1ski.vibits.shared.feature.preferences.data

import platform.Foundation.NSUserDefaults

/**
 * iOS implementation backed by NSUserDefaults.
 */
actual class PreferencesStore {
  private val defaults = NSUserDefaults.standardUserDefaults

  actual fun load(): LocalUserPreferences {
    val defaultTab = LocalUserPreferences.DEFAULT_TIME_RANGE_TAB
    val defaultLanguage = LocalUserPreferences.DEFAULT_LANGUAGE
    val defaultTheme = LocalUserPreferences.DEFAULT_THEME
    val legacyTab = defaults.stringForKey("ui_time_range_tab")
    val habitsTab = defaults.stringForKey("ui_habits_time_range_tab") ?: legacyTab ?: defaultTab
    val postsTab = defaults.stringForKey("ui_posts_time_range_tab") ?: legacyTab ?: defaultTab
    val language = defaults.stringForKey("ui_language") ?: defaultLanguage
    val theme = defaults.stringForKey("ui_theme") ?: defaultTheme
    return LocalUserPreferences(
      habitsTimeRangeTab = habitsTab,
      postsTimeRangeTab = postsTab,
      language = language,
      theme = theme,
    )
  }

  actual fun save(preferences: LocalUserPreferences) {
    defaults.setObject(preferences.habitsTimeRangeTab, forKey = "ui_habits_time_range_tab")
    defaults.setObject(preferences.postsTimeRangeTab, forKey = "ui_posts_time_range_tab")
    defaults.setObject(preferences.language, forKey = "ui_language")
    defaults.setObject(preferences.theme, forKey = "ui_theme")
  }
}
