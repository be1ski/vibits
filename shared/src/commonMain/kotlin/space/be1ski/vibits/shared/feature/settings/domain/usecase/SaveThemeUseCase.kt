package space.be1ski.vibits.shared.feature.settings.domain.usecase

import dev.zacsweers.metro.Inject
import space.be1ski.vibits.shared.feature.settings.domain.model.AppTheme
import space.be1ski.vibits.shared.feature.settings.domain.repository.PreferencesRepository

fun interface SaveTheme {
  operator fun invoke(theme: AppTheme)
}

@Inject
class SaveThemeUseCase(
  private val preferencesRepository: PreferencesRepository,
) : SaveTheme {
  override operator fun invoke(theme: AppTheme) {
    val currentPrefs = preferencesRepository.load()
    val updatedPrefs = currentPrefs.copy(theme = theme)
    preferencesRepository.save(updatedPrefs)
  }
}
