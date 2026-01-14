package space.be1ski.memos.shared.domain.usecase

import space.be1ski.memos.shared.domain.model.preferences.TimeRangeTab
import space.be1ski.memos.shared.domain.repository.PreferencesRepository

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
