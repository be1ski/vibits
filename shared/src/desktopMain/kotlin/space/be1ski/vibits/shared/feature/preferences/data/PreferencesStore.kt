package space.be1ski.vibits.shared.feature.preferences.data

import java.util.prefs.Preferences
import space.be1ski.vibits.shared.data.local.DesktopStoragePaths

/**
 * Desktop implementation backed by Preferences API.
 */
actual class PreferencesStore {
  private val prefs = Preferences.userRoot().node(DesktopStoragePaths.preferencesNode())

  actual fun load(): LocalUserPreferences {
    val defaultTab = LocalUserPreferences.DEFAULT_TIME_RANGE_TAB
    val legacyTab = prefs.get("ui_time_range_tab", null)
    val habitsTab = prefs.get("ui_habits_time_range_tab", legacyTab ?: defaultTab)
    val postsTab = prefs.get("ui_posts_time_range_tab", legacyTab ?: defaultTab)
    return LocalUserPreferences(habitsTimeRangeTab = habitsTab, postsTimeRangeTab = postsTab)
  }

  actual fun save(preferences: LocalUserPreferences) {
    prefs.put("ui_habits_time_range_tab", preferences.habitsTimeRangeTab)
    prefs.put("ui_posts_time_range_tab", preferences.postsTimeRangeTab)
  }
}
