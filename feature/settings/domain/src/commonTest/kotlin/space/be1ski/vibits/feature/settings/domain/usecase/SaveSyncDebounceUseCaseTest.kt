package space.be1ski.vibits.feature.settings.domain.usecase

import space.be1ski.vibits.core.platform.locale.AppLanguage
import space.be1ski.vibits.feature.settings.domain.model.AppTheme
import space.be1ski.vibits.feature.settings.domain.model.TimeRangeTab
import space.be1ski.vibits.feature.settings.domain.model.UserPreferences
import space.be1ski.vibits.feature.settings.domain.test.FakePreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

class SaveSyncDebounceUseCaseTest {
  @Test
  fun `when save sync debounce then updates debounce preference`() {
    val repository = FakePreferencesRepository()
    val useCase = SaveSyncDebounceUseCase(repository)

    useCase(60)

    assertEquals(60.seconds, repository.stored.memosAutoSyncDebounceDuration)
  }

  @Test
  fun `when save sync debounce then preserves other preferences`() {
    val initialPrefs =
      UserPreferences(
        habitsTimeRangeTab = TimeRangeTab.MONTHS,
        postsTimeRangeTab = TimeRangeTab.QUARTERS,
        theme = AppTheme.DARK,
        language = AppLanguage.ENGLISH,
      )
    val repository = FakePreferencesRepository(initialPrefs)
    val useCase = SaveSyncDebounceUseCase(repository)

    useCase(45)

    assertEquals(45.seconds, repository.stored.memosAutoSyncDebounceDuration)
    assertEquals(TimeRangeTab.MONTHS, repository.stored.habitsTimeRangeTab)
    assertEquals(AppLanguage.ENGLISH, repository.stored.language)
    assertEquals(AppTheme.DARK, repository.stored.theme)
  }
}
