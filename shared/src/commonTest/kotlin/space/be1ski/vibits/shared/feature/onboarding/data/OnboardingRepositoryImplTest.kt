package space.be1ski.vibits.shared.feature.onboarding.data

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OnboardingRepositoryImplTest {
  @Test
  fun `when onboarding not completed then returns false`() =
    runTest {
      val store = FakeOnboardingStore(completed = false)
      val dataSource = HabitPresetsDataSourceImpl()
      val repository = OnboardingRepositoryImpl(store, dataSource)

      val result = repository.isOnboardingCompleted()

      assertFalse(result)
    }

  @Test
  fun `when onboarding completed then returns true`() =
    runTest {
      val store = FakeOnboardingStore(completed = true)
      val dataSource = HabitPresetsDataSourceImpl()
      val repository = OnboardingRepositoryImpl(store, dataSource)

      val result = repository.isOnboardingCompleted()

      assertTrue(result)
    }

  @Test
  fun `when mark onboarding completed then persists state`() =
    runTest {
      val store = FakeOnboardingStore(completed = false)
      val dataSource = HabitPresetsDataSourceImpl()
      val repository = OnboardingRepositoryImpl(store, dataSource)

      repository.markOnboardingCompleted()

      assertTrue(repository.isOnboardingCompleted())
    }

  @Test
  fun `when get habit presets then returns all presets`() =
    runTest {
      val store = FakeOnboardingStore()
      val dataSource = HabitPresetsDataSourceImpl()
      val repository = OnboardingRepositoryImpl(store, dataSource)

      val result = repository.getHabitPresets()

      assertEquals(5, result.size)
      assertEquals("water", result[0].id)
      assertEquals("stretch", result[1].id)
      assertEquals("read", result[2].id)
      assertEquals("walk", result[3].id)
      assertEquals("custom", result[4].id)
    }
}
