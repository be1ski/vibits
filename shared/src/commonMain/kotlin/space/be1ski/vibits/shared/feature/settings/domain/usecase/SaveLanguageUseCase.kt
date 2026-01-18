package space.be1ski.vibits.shared.feature.settings.domain.usecase

import dev.zacsweers.metro.Inject
import space.be1ski.vibits.shared.core.platform.locale.LocaleProvider
import space.be1ski.vibits.shared.feature.settings.domain.model.AppLanguage
import space.be1ski.vibits.shared.feature.settings.domain.repository.PreferencesRepository

@Inject
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
