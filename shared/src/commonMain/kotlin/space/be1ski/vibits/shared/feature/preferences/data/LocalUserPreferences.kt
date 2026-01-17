package space.be1ski.vibits.shared.feature.preferences.data

import space.be1ski.vibits.shared.feature.preferences.domain.model.AppLanguage
import space.be1ski.vibits.shared.feature.preferences.domain.model.AppTheme
import space.be1ski.vibits.shared.feature.preferences.domain.model.TimeRangeTab

data class LocalUserPreferences(
  val habitsTimeRangeTab: String,
  val postsTimeRangeTab: String,
  val language: String = DEFAULT_LANGUAGE,
  val theme: String = DEFAULT_THEME,
) {
  companion object {
    val DEFAULT_TIME_RANGE_TAB = TimeRangeTab.WEEKS.name
    val DEFAULT_LANGUAGE = AppLanguage.SYSTEM.name
    val DEFAULT_THEME = AppTheme.SYSTEM.name
  }
}

expect class PreferencesStore() {
  fun load(): LocalUserPreferences

  fun save(preferences: LocalUserPreferences)
}
