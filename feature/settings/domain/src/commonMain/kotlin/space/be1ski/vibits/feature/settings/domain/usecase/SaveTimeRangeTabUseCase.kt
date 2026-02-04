package space.be1ski.vibits.feature.settings.domain.usecase

import dev.zacsweers.metro.Inject
import space.be1ski.vibits.feature.settings.domain.model.TimeRangeScreen
import space.be1ski.vibits.feature.settings.domain.model.TimeRangeTab
import space.be1ski.vibits.feature.settings.domain.repository.PreferencesRepository

@Inject
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
