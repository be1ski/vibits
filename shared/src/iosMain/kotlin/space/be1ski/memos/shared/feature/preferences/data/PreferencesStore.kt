package space.be1ski.memos.shared.feature.preferences.data

import platform.Foundation.NSUserDefaults

/**
 * iOS implementation backed by NSUserDefaults.
 */
actual class PreferencesStore {
  private val defaults = NSUserDefaults.standardUserDefaults

  actual fun load(): LocalUserPreferences {
    val timeRangeTab = defaults.stringForKey("ui_time_range_tab")
      ?: LocalUserPreferences.DEFAULT_TIME_RANGE_TAB
    return LocalUserPreferences(timeRangeTab = timeRangeTab)
  }

  actual fun save(preferences: LocalUserPreferences) {
    defaults.setObject(preferences.timeRangeTab, forKey = "ui_time_range_tab")
  }
}
