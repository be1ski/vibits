package space.be1ski.vibits.shared.feature.settings.domain.usecase

import space.be1ski.vibits.shared.feature.settings.domain.model.TimeRangeTab
import space.be1ski.vibits.shared.feature.settings.domain.model.UserPreferences
import space.be1ski.vibits.shared.test.FakePreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class LoadPreferencesUseCaseTest {
  @Test
  fun `when invoke then returns preferences from repository`() {
    val preferences =
      UserPreferences(
        habitsTimeRangeTab = TimeRangeTab.WEEKS,
        postsTimeRangeTab = TimeRangeTab.WEEKS,
      )
    val repository = FakePreferencesRepository(initial = preferences)
    val useCase = LoadPreferencesUseCase(repository)

    val result = useCase()

    assertEquals(preferences, result)
  }
}
