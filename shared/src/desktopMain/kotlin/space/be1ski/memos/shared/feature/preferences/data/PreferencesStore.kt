package space.be1ski.memos.shared.feature.preferences.data

import java.util.prefs.Preferences
import space.be1ski.memos.shared.data.local.DesktopStoragePaths

/**
 * Desktop implementation backed by Preferences API.
 */
actual class PreferencesStore {
  private val prefs = Preferences.userRoot().node(DesktopStoragePaths.preferencesNode())

  actual fun load(): LocalUserPreferences {
    val timeRangeTab = prefs.get("ui_time_range_tab", LocalUserPreferences.DEFAULT_TIME_RANGE_TAB)
    return LocalUserPreferences(timeRangeTab = timeRangeTab)
  }

  actual fun save(preferences: LocalUserPreferences) {
    prefs.put("ui_time_range_tab", preferences.timeRangeTab)
  }
}
