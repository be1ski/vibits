package space.be1ski.vibits.shared.feature.preferences.data

import space.be1ski.vibits.shared.feature.preferences.domain.model.TimeRangeTab

data class LocalUserPreferences(
  val habitsTimeRangeTab: String,
  val postsTimeRangeTab: String
) {
  companion object {
    val DEFAULT_TIME_RANGE_TAB = TimeRangeTab.Weeks.name
  }
}

expect class PreferencesStore() {
  fun load(): LocalUserPreferences
  fun save(preferences: LocalUserPreferences)
}
