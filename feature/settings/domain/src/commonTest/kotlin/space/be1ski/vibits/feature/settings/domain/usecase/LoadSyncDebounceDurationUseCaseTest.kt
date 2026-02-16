package space.be1ski.vibits.feature.settings.domain.usecase

import space.be1ski.vibits.feature.settings.domain.model.TimeRangeTab
import space.be1ski.vibits.feature.settings.domain.model.UserPreferences
import space.be1ski.vibits.feature.settings.domain.test.FakePreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

class LoadSyncDebounceDurationUseCaseTest {
  @Test
  fun `when invoked then returns debounce duration from preferences`() {
    val prefs = UserPreferences(TimeRangeTab.WEEKS, TimeRangeTab.WEEKS, memosAutoSyncDebounceDuration = 15.seconds)
    val useCase = LoadSyncDebounceDurationUseCase(FakePreferencesRepository(prefs))

    assertEquals(15.seconds, useCase())
  }

  @Test
  fun `when default preferences then returns default debounce duration`() {
    val useCase = LoadSyncDebounceDurationUseCase(FakePreferencesRepository())

    assertEquals(5.seconds, useCase())
  }
}
