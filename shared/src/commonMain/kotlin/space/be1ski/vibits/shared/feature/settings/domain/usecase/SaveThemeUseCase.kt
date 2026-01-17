package space.be1ski.vibits.shared.feature.settings.domain.usecase

import space.be1ski.vibits.shared.feature.settings.domain.model.AppTheme
import space.be1ski.vibits.shared.feature.settings.domain.repository.PreferencesRepository

class SaveThemeUseCase(
  private val preferencesRepository: PreferencesRepository,
) {
  operator fun invoke(theme: AppTheme) {
    val currentPrefs = preferencesRepository.load()
    val updatedPrefs = currentPrefs.copy(theme = theme)
    preferencesRepository.save(updatedPrefs)
  }
}
