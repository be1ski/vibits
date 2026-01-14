package space.be1ski.memos.shared.data.local

import space.be1ski.memos.shared.domain.model.preferences.TimeRangeTab

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
