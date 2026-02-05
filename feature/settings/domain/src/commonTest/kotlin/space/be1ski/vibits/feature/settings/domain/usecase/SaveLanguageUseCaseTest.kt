package space.be1ski.vibits.feature.settings.domain.usecase

import space.be1ski.vibits.core.platform.locale.AppLanguage
import space.be1ski.vibits.core.platform.locale.LocaleProvider
import space.be1ski.vibits.feature.settings.domain.model.AppTheme
import space.be1ski.vibits.feature.settings.domain.model.TimeRangeTab
import space.be1ski.vibits.feature.settings.domain.model.UserPreferences
import space.be1ski.vibits.feature.settings.domain.test.FakePreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class SaveLanguageUseCaseTest {
  @Test
  fun `when save language then updates language preference`() {
    val repository = FakePreferencesRepository()
    val localeProvider = LocaleProvider()
    val useCase = SaveLanguageUseCase(repository, localeProvider)

    useCase(AppLanguage.RUSSIAN)

    assertEquals(AppLanguage.RUSSIAN, repository.stored.language)
  }

  @Test
  fun `when save language then preserves other preferences`() {
    val initialPrefs =
      UserPreferences(
        habitsTimeRangeTab = TimeRangeTab.MONTHS,
        postsTimeRangeTab = TimeRangeTab.QUARTERS,
        theme = AppTheme.DARK,
        language = AppLanguage.ENGLISH,
      )
    val repository = FakePreferencesRepository(initialPrefs)
    val localeProvider = LocaleProvider()
    val useCase = SaveLanguageUseCase(repository, localeProvider)

    useCase(AppLanguage.RUSSIAN)

    assertEquals(AppLanguage.RUSSIAN, repository.stored.language)
    assertEquals(TimeRangeTab.MONTHS, repository.stored.habitsTimeRangeTab)
    assertEquals(AppTheme.DARK, repository.stored.theme)
  }
}
