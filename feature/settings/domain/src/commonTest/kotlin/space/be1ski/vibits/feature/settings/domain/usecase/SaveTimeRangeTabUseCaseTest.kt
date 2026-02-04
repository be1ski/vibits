package space.be1ski.vibits.feature.settings.domain.usecase

import space.be1ski.vibits.core.platform.locale.AppLanguage
import space.be1ski.vibits.feature.main.test.FakePreferencesRepository
import space.be1ski.vibits.feature.settings.domain.model.AppTheme
import space.be1ski.vibits.feature.settings.domain.model.TimeRangeScreen
import space.be1ski.vibits.feature.settings.domain.model.TimeRangeTab
import space.be1ski.vibits.feature.settings.domain.model.UserPreferences
import kotlin.test.Test
import kotlin.test.assertEquals

class SaveTimeRangeTabUseCaseTest {
  @Test
  fun `when save HABITS screen tab then updates habitsTimeRangeTab`() {
    val repository = FakePreferencesRepository()
    val useCase = SaveTimeRangeTabUseCase(repository)

    useCase(TimeRangeScreen.HABITS, TimeRangeTab.MONTHS)

    assertEquals(TimeRangeTab.MONTHS, repository.stored.habitsTimeRangeTab)
    assertEquals(TimeRangeTab.WEEKS, repository.stored.postsTimeRangeTab)
  }

  @Test
  fun `when save POSTS screen tab then updates postsTimeRangeTab`() {
    val repository = FakePreferencesRepository()
    val useCase = SaveTimeRangeTabUseCase(repository)

    useCase(TimeRangeScreen.POSTS, TimeRangeTab.QUARTERS)

    assertEquals(TimeRangeTab.WEEKS, repository.stored.habitsTimeRangeTab)
    assertEquals(TimeRangeTab.QUARTERS, repository.stored.postsTimeRangeTab)
  }

  @Test
  fun `when save then preserves other preferences`() {
    val initialPrefs =
      UserPreferences(
        habitsTimeRangeTab = TimeRangeTab.WEEKS,
        postsTimeRangeTab = TimeRangeTab.MONTHS,
        theme = AppTheme.DARK,
        language = AppLanguage.RUSSIAN,
      )
    val repository = FakePreferencesRepository(initialPrefs)
    val useCase = SaveTimeRangeTabUseCase(repository)

    useCase(TimeRangeScreen.HABITS, TimeRangeTab.YEARS)

    assertEquals(TimeRangeTab.YEARS, repository.stored.habitsTimeRangeTab)
    assertEquals(AppTheme.DARK, repository.stored.theme)
    assertEquals(AppLanguage.RUSSIAN, repository.stored.language)
  }
}
