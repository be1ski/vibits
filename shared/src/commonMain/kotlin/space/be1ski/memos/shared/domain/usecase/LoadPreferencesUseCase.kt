package space.be1ski.memos.shared.domain.usecase

import space.be1ski.memos.shared.domain.model.preferences.UserPreferences
import space.be1ski.memos.shared.domain.repository.PreferencesRepository

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
