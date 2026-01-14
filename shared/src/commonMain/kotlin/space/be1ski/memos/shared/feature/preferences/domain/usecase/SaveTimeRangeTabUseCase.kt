package space.be1ski.memos.shared.feature.preferences.domain.usecase

import space.be1ski.memos.shared.feature.preferences.domain.model.TimeRangeTab
import space.be1ski.memos.shared.feature.preferences.domain.repository.PreferencesRepository

internal class SaveTimeRangeTabUseCase(
  private val preferencesRepository: PreferencesRepository
) {
  operator fun invoke(timeRangeTab: TimeRangeTab) {
    val currentPrefs = preferencesRepository.load()
    val updatedPrefs = currentPrefs.copy(selectedTimeRangeTab = timeRangeTab)
    preferencesRepository.save(updatedPrefs)
  }
}
