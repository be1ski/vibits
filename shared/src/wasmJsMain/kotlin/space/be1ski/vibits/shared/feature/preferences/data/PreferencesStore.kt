package space.be1ski.vibits.shared.feature.preferences.data

import kotlinx.browser.localStorage

private const val KEY_HABITS_TAB = "vibits_habits_time_range_tab"
private const val KEY_POSTS_TAB = "vibits_posts_time_range_tab"

/**
 * Web implementation storing preferences in localStorage.
 */
actual class PreferencesStore {
  actual fun load(): LocalUserPreferences {
    val defaultTab = LocalUserPreferences.DEFAULT_TIME_RANGE_TAB
    val habitsTab = localStorage.getItem(KEY_HABITS_TAB) ?: defaultTab
    val postsTab = localStorage.getItem(KEY_POSTS_TAB) ?: defaultTab
    return LocalUserPreferences(habitsTimeRangeTab = habitsTab, postsTimeRangeTab = postsTab)
  }

  actual fun save(preferences: LocalUserPreferences) {
    localStorage.setItem(KEY_HABITS_TAB, preferences.habitsTimeRangeTab)
    localStorage.setItem(KEY_POSTS_TAB, preferences.postsTimeRangeTab)
  }
}
