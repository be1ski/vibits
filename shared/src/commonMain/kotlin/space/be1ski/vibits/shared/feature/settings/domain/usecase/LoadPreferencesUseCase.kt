package space.be1ski.vibits.shared.feature.settings.domain.usecase

import javax.inject.Inject
import space.be1ski.vibits.shared.feature.settings.domain.model.UserPreferences
import space.be1ski.vibits.shared.feature.settings.domain.repository.PreferencesRepository

class LoadPreferencesUseCase @Inject constructor(
  private val preferencesRepository: PreferencesRepository,
) {
  operator fun invoke(): UserPreferences = preferencesRepository.load()
}
