package space.be1ski.vibits.feature.onboarding.data

import kotlinx.coroutines.test.runTest
import space.be1ski.vibits.feature.homescreen.test.FakeOfflineMemoStorage
import space.be1ski.vibits.feature.homescreen.test.FakeOnboardingStore
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
      val storage = FakeOfflineMemoStorage()
      val repository = OnboardingRepositoryImpl(store, dataSource, storage)

      val result = repository.isOnboardingCompleted()

      assertFalse(result)
    }

  @Test
  fun `when onboarding completed then returns true`() =
    runTest {
      val store = FakeOnboardingStore(completed = true)
      val dataSource = HabitPresetsDataSourceImpl()
      val storage = FakeOfflineMemoStorage()
      val repository = OnboardingRepositoryImpl(store, dataSource, storage)

      val result = repository.isOnboardingCompleted()

      assertTrue(result)
    }

  @Test
  fun `when mark onboarding completed then persists state`() =
    runTest {
      val store = FakeOnboardingStore(completed = false)
      val dataSource = HabitPresetsDataSourceImpl()
      val storage = FakeOfflineMemoStorage()
      val repository = OnboardingRepositoryImpl(store, dataSource, storage)

      repository.markOnboardingCompleted()

      assertTrue(repository.isOnboardingCompleted())
    }

  @Test
  fun `when get habit presets then returns all presets`() =
    runTest {
      val store = FakeOnboardingStore()
      val dataSource = HabitPresetsDataSourceImpl()
      val storage = FakeOfflineMemoStorage()
      val repository = OnboardingRepositoryImpl(store, dataSource, storage)

      val result = repository.getHabitPresets()

      assertEquals(9, result.size)
      assertEquals("exercise", result[0].id)
      assertEquals("water", result[1].id)
      assertEquals("reading", result[2].id)
      assertEquals("meditation", result[3].id)
      assertEquals("walking", result[4].id)
      assertEquals("learning", result[5].id)
      assertEquals("no_sugar", result[6].id)
      assertEquals("early_sleep", result[7].id)
      assertEquals("custom", result[8].id)
    }
}
