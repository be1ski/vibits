package space.be1ski.vibits.shared.feature.settings.domain.usecase

import space.be1ski.vibits.shared.feature.settings.domain.model.AppLanguage
import space.be1ski.vibits.shared.feature.settings.domain.model.AppTheme
import space.be1ski.vibits.shared.feature.settings.domain.model.TimeRangeTab
import space.be1ski.vibits.shared.feature.settings.domain.model.UserPreferences
import space.be1ski.vibits.shared.feature.settings.domain.repository.PreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class SaveTimeRangeTabUseCaseTest {
  private class FakePreferencesRepository(
    private var prefs: UserPreferences =
      UserPreferences(
        habitsTimeRangeTab = TimeRangeTab.WEEKS,
        postsTimeRangeTab = TimeRangeTab.WEEKS,
      ),
  ) : PreferencesRepository {
    override fun load(): UserPreferences = prefs

    override fun save(preferences: UserPreferences) {
      prefs = preferences
    }
  }

  @Test
  fun `when save HABITS screen tab then updates habitsTimeRangeTab`() {
    val repository = FakePreferencesRepository()
    val useCase = SaveTimeRangeTabUseCase(repository)

    useCase(TimeRangeScreen.HABITS, TimeRangeTab.MONTHS)

    assertEquals(TimeRangeTab.MONTHS, repository.load().habitsTimeRangeTab)
    assertEquals(TimeRangeTab.WEEKS, repository.load().postsTimeRangeTab) // Unchanged
  }

  @Test
  fun `when save POSTS screen tab then updates postsTimeRangeTab`() {
    val repository = FakePreferencesRepository()
    val useCase = SaveTimeRangeTabUseCase(repository)

    useCase(TimeRangeScreen.POSTS, TimeRangeTab.QUARTERS)

    assertEquals(TimeRangeTab.WEEKS, repository.load().habitsTimeRangeTab) // Unchanged
    assertEquals(TimeRangeTab.QUARTERS, repository.load().postsTimeRangeTab)
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

    val saved = repository.load()
    assertEquals(TimeRangeTab.YEARS, saved.habitsTimeRangeTab)
    assertEquals(AppTheme.DARK, saved.theme) // Preserved
    assertEquals(AppLanguage.RUSSIAN, saved.language) // Preserved
  }
}

class SaveThemeUseCaseTest {
  private class FakePreferencesRepository(
    private var prefs: UserPreferences =
      UserPreferences(
        habitsTimeRangeTab = TimeRangeTab.WEEKS,
        postsTimeRangeTab = TimeRangeTab.WEEKS,
      ),
  ) : PreferencesRepository {
    override fun load(): UserPreferences = prefs

    override fun save(preferences: UserPreferences) {
      prefs = preferences
    }
  }

  @Test
  fun `when save theme then updates theme preference`() {
    val repository = FakePreferencesRepository()
    val useCase = SaveThemeUseCase(repository)

    useCase(AppTheme.DARK)

    assertEquals(AppTheme.DARK, repository.load().theme)
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

    val saved = repository.load()
    assertEquals(AppTheme.SYSTEM, saved.theme)
    assertEquals(TimeRangeTab.MONTHS, saved.habitsTimeRangeTab)
    assertEquals(AppLanguage.ENGLISH, saved.language)
  }
}
