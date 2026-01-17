package space.be1ski.vibits.shared.feature.settings.domain.usecase

import javax.inject.Inject
import space.be1ski.vibits.shared.feature.settings.domain.model.AppTheme
import space.be1ski.vibits.shared.feature.settings.domain.repository.PreferencesRepository

class SaveThemeUseCase @Inject constructor(
  private val preferencesRepository: PreferencesRepository,
) {
  operator fun invoke(theme: AppTheme) {
    val currentPrefs = preferencesRepository.load()
    val updatedPrefs = currentPrefs.copy(theme = theme)
    preferencesRepository.save(updatedPrefs)
  }
}
