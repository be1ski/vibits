package space.be1ski.vibits.shared.feature.preferences.domain.usecase

import space.be1ski.vibits.shared.feature.preferences.domain.model.UserPreferences
import space.be1ski.vibits.shared.feature.preferences.domain.repository.PreferencesRepository

internal class LoadPreferencesUseCase(
  private val preferencesRepository: PreferencesRepository
) {
  operator fun invoke(): UserPreferences = preferencesRepository.load()
}
