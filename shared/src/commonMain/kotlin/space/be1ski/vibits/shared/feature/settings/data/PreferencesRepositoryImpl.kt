package space.be1ski.vibits.shared.feature.settings.data

import dev.zacsweers.metro.Inject
import space.be1ski.vibits.shared.core.logging.Log
import space.be1ski.vibits.shared.feature.settings.domain.model.AppLanguage
import space.be1ski.vibits.shared.feature.settings.domain.model.AppTheme
import space.be1ski.vibits.shared.feature.settings.domain.model.TimeRangeTab
import space.be1ski.vibits.shared.feature.settings.domain.model.UserPreferences
import space.be1ski.vibits.shared.feature.settings.domain.repository.PreferencesRepository

private const val TAG = "Preferences"

/**
 * Repository implementation backed by platform preferences storage.
 */
@Inject
class PreferencesRepositoryImpl(
  private val preferencesStore: PreferencesStore,
) : PreferencesRepository {
  override fun load(): UserPreferences {
    val local = preferencesStore.load()
    val habitsTab = parseTimeRangeTab(local.habitsTimeRangeTab)
    val postsTab = parseTimeRangeTab(local.postsTimeRangeTab)
    val language = parseLanguage(local.language)
    val theme = parseTheme(local.theme)
    Log.d(TAG, "Loaded: theme=$theme, lang=$language")
    return UserPreferences(
      habitsTimeRangeTab = habitsTab,
      postsTimeRangeTab = postsTab,
      language = language,
      theme = theme,
    )
  }

  override fun save(preferences: UserPreferences) {
    Log.i(TAG, "Saving: theme=${preferences.theme}, lang=${preferences.language}")
    val local =
      LocalUserPreferences(
        habitsTimeRangeTab = preferences.habitsTimeRangeTab.name,
        postsTimeRangeTab = preferences.postsTimeRangeTab.name,
        language = preferences.language.name,
        theme = preferences.theme.name,
      )
    preferencesStore.save(local)
  }

  private fun parseTimeRangeTab(value: String): TimeRangeTab = runCatching { TimeRangeTab.valueOf(value) }.getOrDefault(TimeRangeTab.WEEKS)

  private fun parseLanguage(value: String): AppLanguage = runCatching { AppLanguage.valueOf(value) }.getOrDefault(AppLanguage.SYSTEM)

  private fun parseTheme(value: String): AppTheme = runCatching { AppTheme.valueOf(value) }.getOrDefault(AppTheme.SYSTEM)
}
