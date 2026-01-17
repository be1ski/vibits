package space.be1ski.vibits.shared.feature.preferences.domain.usecase

import space.be1ski.vibits.shared.feature.preferences.domain.model.AppTheme
import space.be1ski.vibits.shared.feature.preferences.domain.repository.PreferencesRepository

class SaveThemeUseCase(
  private val preferencesRepository: PreferencesRepository,
) {
  operator fun invoke(theme: AppTheme) {
    val currentPrefs = preferencesRepository.load()
    val updatedPrefs = currentPrefs.copy(theme = theme)
    preferencesRepository.save(updatedPrefs)
  }
}
