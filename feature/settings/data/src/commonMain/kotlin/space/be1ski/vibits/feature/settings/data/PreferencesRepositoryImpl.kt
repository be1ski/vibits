package space.be1ski.vibits.feature.settings.data

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import space.be1ski.vibits.core.platform.di.AppScope
import space.be1ski.vibits.core.platform.locale.AppLanguage
import space.be1ski.vibits.core.utils.logging.Log
import space.be1ski.vibits.feature.settings.domain.model.AppTheme
import space.be1ski.vibits.feature.settings.domain.model.DEFAULT_MEMOS_AUTO_SYNC_DEBOUNCE_DURATION
import space.be1ski.vibits.feature.settings.domain.model.TimeRangeTab
import space.be1ski.vibits.feature.settings.domain.model.UserPreferences
import space.be1ski.vibits.feature.settings.domain.repository.PreferencesRepository
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private const val TAG = "Preferences"

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
internal class PreferencesRepositoryImpl(
  private val preferencesStore: PreferencesStore,
) : PreferencesRepository {
  override fun load(): UserPreferences {
    val local = preferencesStore.load()
    val habitsTab = parseTimeRangeTab(local.habitsTimeRangeTab)
    val postsTab = parseTimeRangeTab(local.postsTimeRangeTab)
    val language = parseLanguage(local.language)
    val theme = parseTheme(local.theme)
    val memosAutoSyncDebounceDuration = parseMemosAutoSyncDebounceDuration(local.memosAutoSyncDebounceSeconds)
    Log.d(TAG, "Loaded: theme=$theme, lang=$language, syncDebounce=${memosAutoSyncDebounceDuration.inWholeSeconds}s")
    return UserPreferences(
      habitsTimeRangeTab = habitsTab,
      postsTimeRangeTab = postsTab,
      language = language,
      theme = theme,
      memosAutoSyncDebounceDuration = memosAutoSyncDebounceDuration,
    )
  }

  override fun save(preferences: UserPreferences) {
    Log.i(
      TAG,
      "Saving: theme=${preferences.theme}, lang=${preferences.language}, " +
        "syncDebounce=${preferences.memosAutoSyncDebounceDuration.inWholeSeconds}s",
    )
    val local =
      LocalUserPreferences(
        habitsTimeRangeTab = preferences.habitsTimeRangeTab.name,
        postsTimeRangeTab = preferences.postsTimeRangeTab.name,
        language = preferences.language.name,
        theme = preferences.theme.name,
        memosAutoSyncDebounceSeconds = preferences.memosAutoSyncDebounceDuration.inWholeSeconds,
      )
    preferencesStore.save(local)
  }

  private fun parseTimeRangeTab(value: String): TimeRangeTab = runCatching { TimeRangeTab.valueOf(value) }.getOrDefault(TimeRangeTab.WEEKS)

  private fun parseLanguage(value: String): AppLanguage = runCatching { AppLanguage.valueOf(value) }.getOrDefault(AppLanguage.SYSTEM)

  private fun parseTheme(value: String): AppTheme = runCatching { AppTheme.valueOf(value) }.getOrDefault(AppTheme.SYSTEM)

  private fun parseMemosAutoSyncDebounceDuration(value: Long): Duration =
    if (value > 0) value.seconds else DEFAULT_MEMOS_AUTO_SYNC_DEBOUNCE_DURATION
}
