package space.be1ski.memos.shared.feature.preferences.data

import space.be1ski.memos.shared.feature.preferences.domain.model.TimeRangeTab

data class LocalUserPreferences(
  val timeRangeTab: String
) {
  companion object {
    val DEFAULT_TIME_RANGE_TAB = TimeRangeTab.Weeks.name
  }
}

expect class PreferencesStore() {
  fun load(): LocalUserPreferences
  fun save(preferences: LocalUserPreferences)
}
