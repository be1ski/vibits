package space.be1ski.vibits.shared.feature.settings.domain.usecase

import space.be1ski.vibits.shared.feature.settings.domain.model.UserPreferences
import space.be1ski.vibits.shared.feature.settings.domain.repository.PreferencesRepository

internal class LoadPreferencesUseCase(
  private val preferencesRepository: PreferencesRepository,
) {
  operator fun invoke(): UserPreferences = preferencesRepository.load()
}
