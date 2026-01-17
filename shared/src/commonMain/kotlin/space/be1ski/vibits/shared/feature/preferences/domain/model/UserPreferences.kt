package space.be1ski.vibits.shared.feature.preferences.domain.model

/**
 * Domain model for user UI preferences.
 */
data class UserPreferences(
  val habitsTimeRangeTab: TimeRangeTab,
  val postsTimeRangeTab: TimeRangeTab,
)
