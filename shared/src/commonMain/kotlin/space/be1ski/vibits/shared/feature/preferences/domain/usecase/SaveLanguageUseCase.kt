package space.be1ski.vibits.shared.feature.preferences.domain.usecase

import space.be1ski.vibits.shared.core.platform.LocaleProvider
import space.be1ski.vibits.shared.feature.preferences.domain.model.AppLanguage
import space.be1ski.vibits.shared.feature.preferences.domain.repository.PreferencesRepository

/**
 * Saves the selected language preference and configures the locale.
 * @return true if a restart is required for the change to take effect
 */
class SaveLanguageUseCase(
  private val preferencesRepository: PreferencesRepository,
  private val localeProvider: LocaleProvider,
) {
  operator fun invoke(language: AppLanguage): Boolean {
    val currentPrefs = preferencesRepository.load()
    val updatedPrefs = currentPrefs.copy(language = language)
    preferencesRepository.save(updatedPrefs)
    return localeProvider.configureLocale(language)
  }
}
