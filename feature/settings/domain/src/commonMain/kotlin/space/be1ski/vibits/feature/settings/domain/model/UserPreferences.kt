package space.be1ski.vibits.feature.settings.domain.model

import space.be1ski.vibits.core.platform.locale.AppLanguage

/**
 * Domain model for user UI preferences.
 */
data class UserPreferences(
  val habitsTimeRangeTab: TimeRangeTab,
  val postsTimeRangeTab: TimeRangeTab,
  val language: AppLanguage = AppLanguage.SYSTEM,
  val theme: AppTheme = AppTheme.SYSTEM,
)
