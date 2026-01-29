package space.be1ski.vibits.shared.app.presentation

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import space.be1ski.vibits.shared.app.presentation.handler.AppEffectHandler
import space.be1ski.vibits.shared.feature.settings.domain.model.TimeRangeTab
import space.be1ski.vibits.shared.feature.settings.domain.usecase.SaveTimeRangeTabUseCase
import space.be1ski.vibits.shared.test.FakePreferencesRepository
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
