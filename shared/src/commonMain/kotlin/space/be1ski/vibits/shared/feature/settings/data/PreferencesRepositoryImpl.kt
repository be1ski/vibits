package space.be1ski.vibits.shared.feature.settings.data

import javax.inject.Inject
import space.be1ski.vibits.shared.feature.settings.domain.model.AppLanguage
import space.be1ski.vibits.shared.feature.settings.domain.model.AppTheme
import space.be1ski.vibits.shared.feature.settings.domain.model.TimeRangeTab
import space.be1ski.vibits.shared.feature.settings.domain.model.UserPreferences
import space.be1ski.vibits.shared.feature.settings.domain.repository.PreferencesRepository

/**
 * Repository implementation backed by platform preferences storage.
 */
class PreferencesRepositoryImpl @Inject constructor(
  private val preferencesStore: PreferencesStore,
) : PreferencesRepository {
  override fun load(): UserPreferences {
    val local = preferencesStore.load()
    val habitsTab = parseTimeRangeTab(local.habitsTimeRangeTab)
    val postsTab = parseTimeRangeTab(local.postsTimeRangeTab)
    val language = parseLanguage(local.language)
    val theme = parseTheme(local.theme)
    return UserPreferences(
      habitsTimeRangeTab = habitsTab,
      postsTimeRangeTab = postsTab,
      language = language,
      theme = theme,
    )
  }

  override fun save(preferences: UserPreferences) {
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
