package space.be1ski.vibits.shared.feature.settings.domain.usecase

import space.be1ski.vibits.shared.feature.settings.domain.model.TimeRangeTab
import space.be1ski.vibits.shared.feature.settings.domain.repository.PreferencesRepository

enum class TimeRangeScreen { HABITS, POSTS }

class SaveTimeRangeTabUseCase(
  private val preferencesRepository: PreferencesRepository,
) {
  operator fun invoke(
    screen: TimeRangeScreen,
    timeRangeTab: TimeRangeTab,
  ) {
    val currentPrefs = preferencesRepository.load()
    val updatedPrefs =
      when (screen) {
        TimeRangeScreen.HABITS -> currentPrefs.copy(habitsTimeRangeTab = timeRangeTab)
        TimeRangeScreen.POSTS -> currentPrefs.copy(postsTimeRangeTab = timeRangeTab)
      }
    preferencesRepository.save(updatedPrefs)
  }
}
