package space.be1ski.vibits.shared.feature.settings.domain.usecase

import space.be1ski.vibits.shared.feature.settings.domain.model.AppLanguage
import space.be1ski.vibits.shared.feature.settings.domain.model.AppTheme
import space.be1ski.vibits.shared.feature.settings.domain.model.TimeRangeTab
import space.be1ski.vibits.shared.feature.settings.domain.model.UserPreferences
import space.be1ski.vibits.shared.test.FakePreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class SaveThemeUseCaseTest {
  @Test
  fun `when save theme then updates theme preference`() {
    val repository = FakePreferencesRepository()
    val useCase = SaveThemeUseCase(repository)

    useCase(AppTheme.DARK)

    assertEquals(AppTheme.DARK, repository.stored.theme)
  }

  @Test
  fun `when save theme then preserves other preferences`() {
    val initialPrefs =
      UserPreferences(
        habitsTimeRangeTab = TimeRangeTab.MONTHS,
        postsTimeRangeTab = TimeRangeTab.QUARTERS,
        theme = AppTheme.LIGHT,
        language = AppLanguage.ENGLISH,
      )
    val repository = FakePreferencesRepository(initialPrefs)
    val useCase = SaveThemeUseCase(repository)

    useCase(AppTheme.SYSTEM)

    assertEquals(AppTheme.SYSTEM, repository.stored.theme)
    assertEquals(TimeRangeTab.MONTHS, repository.stored.habitsTimeRangeTab)
    assertEquals(AppLanguage.ENGLISH, repository.stored.language)
  }
}
