package space.be1ski.vibits.feature.settings.data

import space.be1ski.vibits.core.platform.locale.AppLanguage
import space.be1ski.vibits.feature.settings.domain.model.AppTheme
import space.be1ski.vibits.feature.settings.domain.model.DEFAULT_SYNC_DEBOUNCE_SECONDS
import space.be1ski.vibits.feature.settings.domain.model.TimeRangeTab

data class LocalUserPreferences(
  val habitsTimeRangeTab: String,
  val postsTimeRangeTab: String,
  val language: String = DEFAULT_LANGUAGE,
  val theme: String = DEFAULT_THEME,
  val memosAutoSyncDebounceSeconds: Long = DEFAULT_SYNC_DEBOUNCE_SECONDS.toLong(),
) {
  companion object {
    val DEFAULT_TIME_RANGE_TAB = TimeRangeTab.WEEKS.name
    val DEFAULT_LANGUAGE = AppLanguage.SYSTEM.name
    val DEFAULT_THEME = AppTheme.SYSTEM.name
  }
}
