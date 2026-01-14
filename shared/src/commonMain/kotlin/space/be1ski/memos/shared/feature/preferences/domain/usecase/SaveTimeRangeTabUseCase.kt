package space.be1ski.memos.shared.feature.preferences.domain.usecase

import space.be1ski.memos.shared.feature.preferences.domain.model.TimeRangeTab
import space.be1ski.memos.shared.feature.preferences.domain.repository.PreferencesRepository

/**
 * Persists the selected time range tab.
 */
internal class SaveTimeRangeTabUseCase(
  private val preferencesRepository: PreferencesRepository
) {
  /**
   * Saves the selected time range tab to preferences.
   */
  operator fun invoke(timeRangeTab: TimeRangeTab) {
    val currentPrefs = preferencesRepository.load()
    val updatedPrefs = currentPrefs.copy(selectedTimeRangeTab = timeRangeTab)
    preferencesRepository.save(updatedPrefs)
  }
}
