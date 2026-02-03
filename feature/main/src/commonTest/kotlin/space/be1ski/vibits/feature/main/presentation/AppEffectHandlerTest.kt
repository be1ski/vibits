package space.be1ski.vibits.feature.main.presentation
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import space.be1ski.vibits.feature.main.presentation.effect.AppEffect
import space.be1ski.vibits.feature.main.presentation.effect.AppEffectHandler
import space.be1ski.vibits.feature.main.test.FakePreferencesRepository
import space.be1ski.vibits.feature.settings.domain.model.TimeRangeTab
import space.be1ski.vibits.feature.settings.domain.usecase.SaveTimeRangeTabUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppEffectHandlerTest {
  @Test
  fun `when SaveHabitsTimeRangeTab then saves tab for HABITS screen`() =
    runTest {
      val prefsRepo = FakePreferencesRepository()
      val handler = AppEffectHandler(SaveTimeRangeTabUseCase(prefsRepo))

      val actions = handler(AppEffect.SaveHabitsTimeRangeTab(TimeRangeTab.MONTHS)).toList()

      assertTrue(actions.isEmpty())
      assertEquals(TimeRangeTab.MONTHS, prefsRepo.stored.habitsTimeRangeTab)
    }

  @Test
  fun `when SavePostsTimeRangeTab then saves tab for POSTS screen`() =
    runTest {
      val prefsRepo = FakePreferencesRepository()
      val handler = AppEffectHandler(SaveTimeRangeTabUseCase(prefsRepo))

      val actions = handler(AppEffect.SavePostsTimeRangeTab(TimeRangeTab.QUARTERS)).toList()

      assertTrue(actions.isEmpty())
      assertEquals(TimeRangeTab.QUARTERS, prefsRepo.stored.postsTimeRangeTab)
    }
}
