package space.be1ski.vibits.feature.settings.domain.usecase

import dev.zacsweers.metro.Inject
import space.be1ski.vibits.feature.settings.domain.model.UserPreferences
import space.be1ski.vibits.feature.settings.domain.repository.PreferencesRepository

@Inject
class LoadPreferencesUseCase(
  private val preferencesRepository: PreferencesRepository,
) {
  operator fun invoke(): UserPreferences = preferencesRepository.load()
}
