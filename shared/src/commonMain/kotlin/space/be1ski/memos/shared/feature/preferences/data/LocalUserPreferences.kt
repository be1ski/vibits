package space.be1ski.memos.shared.feature.preferences.data

import space.be1ski.memos.shared.feature.preferences.domain.model.TimeRangeTab

/**
 * DTO for persisted user preferences.
 */
data class LocalUserPreferences(
  val timeRangeTab: String
) {
  companion object {
    val DEFAULT_TIME_RANGE_TAB = TimeRangeTab.Weeks.name
  }
}

/**
 * Platform-specific UI preferences storage.
 */
expect class PreferencesStore() {
  /**
   * Loads saved preferences or default values when not available.
   */
  fun load(): LocalUserPreferences

  /**
   * Persists user preferences.
   */
  fun save(preferences: LocalUserPreferences)
}
