package space.be1ski.vibits.shared.feature.preferences.domain.usecase

import space.be1ski.vibits.shared.feature.preferences.domain.model.TimeRangeTab
import space.be1ski.vibits.shared.feature.preferences.domain.repository.PreferencesRepository

internal enum class TimeRangeScreen { Habits, Posts }

internal class SaveTimeRangeTabUseCase(
  private val preferencesRepository: PreferencesRepository
) {
  operator fun invoke(screen: TimeRangeScreen, timeRangeTab: TimeRangeTab) {
    val currentPrefs = preferencesRepository.load()
    val updatedPrefs = when (screen) {
      TimeRangeScreen.Habits -> currentPrefs.copy(habitsTimeRangeTab = timeRangeTab)
      TimeRangeScreen.Posts -> currentPrefs.copy(postsTimeRangeTab = timeRangeTab)
    }
    preferencesRepository.save(updatedPrefs)
  }
}
