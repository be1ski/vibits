package space.be1ski.vibits.feature.settings.domain.model

import space.be1ski.vibits.core.platform.locale.AppLanguage
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

const val MIN_SYNC_DEBOUNCE_SECONDS = 1
const val MAX_SYNC_DEBOUNCE_SECONDS = 30
const val DEFAULT_SYNC_DEBOUNCE_SECONDS = 5

val DEFAULT_MEMOS_AUTO_SYNC_DEBOUNCE_DURATION: Duration = DEFAULT_SYNC_DEBOUNCE_SECONDS.seconds

data class UserPreferences(
  val habitsTimeRangeTab: TimeRangeTab,
  val postsTimeRangeTab: TimeRangeTab,
  val language: AppLanguage = AppLanguage.SYSTEM,
  val theme: AppTheme = AppTheme.SYSTEM,
  val memosAutoSyncDebounceDuration: Duration = DEFAULT_MEMOS_AUTO_SYNC_DEBOUNCE_DURATION,
)
