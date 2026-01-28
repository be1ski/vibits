package space.be1ski.vibits.shared.feature.onboarding.domain.usecase

import kotlinx.coroutines.test.runTest
import space.be1ski.vibits.shared.feature.onboarding.data.FakeOnboardingStore
import space.be1ski.vibits.shared.feature.onboarding.data.HabitPresetsDataSourceImpl
import space.be1ski.vibits.shared.feature.onboarding.data.OnboardingRepositoryImpl
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OnboardingUseCasesTest {
  @Test
  fun `when is onboarding completed then returns correct state`() =
    runTest {
      val store = FakeOnboardingStore(completed = false)
      val dataSource = HabitPresetsDataSourceImpl()
      val repository = OnboardingRepositoryImpl(store, dataSource)
      val useCase = IsOnboardingCompletedUseCase(repository)

      val result = useCase()

      assertFalse(result)
    }

  @Test
  fun `when mark onboarding completed then persists completion`() =
    runTest {
      val store = FakeOnboardingStore(completed = false)
      val dataSource = HabitPresetsDataSourceImpl()
      val repository = OnboardingRepositoryImpl(store, dataSource)
      val markCompleted = MarkOnboardingCompletedUseCase(repository)
      val isCompleted = IsOnboardingCompletedUseCase(repository)

      markCompleted()

      assertTrue(isCompleted())
    }

  @Test
  fun `when get habit presets then returns all presets`() =
    runTest {
      val store = FakeOnboardingStore()
      val dataSource = HabitPresetsDataSourceImpl()
      val repository = OnboardingRepositoryImpl(store, dataSource)
      val useCase = GetHabitPresetsUseCase(repository)

      val result = useCase()

      assertEquals(5, result.size)
      assertTrue(result.any { it.id == "water" })
      assertTrue(result.any { it.id == "stretch" })
      assertTrue(result.any { it.id == "read" })
      assertTrue(result.any { it.id == "walk" })
      assertTrue(result.any { it.id == "custom" })
    }
}
