package space.be1ski.vibits.shared.feature.preferences.data

import platform.Foundation.NSUserDefaults

/**
 * iOS implementation backed by NSUserDefaults.
 */
actual class PreferencesStore {
  private val defaults = NSUserDefaults.standardUserDefaults

  actual fun load(): LocalUserPreferences {
    val defaultTab = LocalUserPreferences.DEFAULT_TIME_RANGE_TAB
    val legacyTab = defaults.stringForKey("ui_time_range_tab")
    val habitsTab = defaults.stringForKey("ui_habits_time_range_tab") ?: legacyTab ?: defaultTab
    val postsTab = defaults.stringForKey("ui_posts_time_range_tab") ?: legacyTab ?: defaultTab
    return LocalUserPreferences(habitsTimeRangeTab = habitsTab, postsTimeRangeTab = postsTab)
  }

  actual fun save(preferences: LocalUserPreferences) {
    defaults.setObject(preferences.habitsTimeRangeTab, forKey = "ui_habits_time_range_tab")
    defaults.setObject(preferences.postsTimeRangeTab, forKey = "ui_posts_time_range_tab")
  }
}
