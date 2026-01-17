package space.be1ski.vibits.shared.feature.settings.data

import kotlinx.browser.localStorage

private const val KEY_HABITS_TAB = "vibits_habits_time_range_tab"
private const val KEY_POSTS_TAB = "vibits_posts_time_range_tab"
private const val KEY_LANGUAGE = "vibits_language"
private const val KEY_THEME = "vibits_theme"

/**
 * Web implementation storing preferences in localStorage.
 */
actual class PreferencesStore {
  actual fun load(): LocalUserPreferences {
    val defaultTab = LocalUserPreferences.DEFAULT_TIME_RANGE_TAB
    val defaultLanguage = LocalUserPreferences.DEFAULT_LANGUAGE
    val defaultTheme = LocalUserPreferences.DEFAULT_THEME
    val habitsTab = localStorage.getItem(KEY_HABITS_TAB) ?: defaultTab
    val postsTab = localStorage.getItem(KEY_POSTS_TAB) ?: defaultTab
    val language = localStorage.getItem(KEY_LANGUAGE) ?: defaultLanguage
    val theme = localStorage.getItem(KEY_THEME) ?: defaultTheme
    return LocalUserPreferences(
      habitsTimeRangeTab = habitsTab,
      postsTimeRangeTab = postsTab,
      language = language,
      theme = theme,
    )
  }

  actual fun save(preferences: LocalUserPreferences) {
    localStorage.setItem(KEY_HABITS_TAB, preferences.habitsTimeRangeTab)
    localStorage.setItem(KEY_POSTS_TAB, preferences.postsTimeRangeTab)
    localStorage.setItem(KEY_LANGUAGE, preferences.language)
    localStorage.setItem(KEY_THEME, preferences.theme)
  }
}
