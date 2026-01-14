package space.be1ski.memos.shared.feature.preferences.domain.usecase

import space.be1ski.memos.shared.feature.preferences.domain.model.UserPreferences
import space.be1ski.memos.shared.feature.preferences.domain.repository.PreferencesRepository

/**
 * Loads locally stored user preferences.
 */
internal class LoadPreferencesUseCase(
  private val preferencesRepository: PreferencesRepository
) {
  /**
   * Returns stored preferences.
   */
  operator fun invoke(): UserPreferences = preferencesRepository.load()
}
