package space.be1ski.vibits.shared.app

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import space.be1ski.vibits.shared.core.platform.LocaleProvider
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode
import space.be1ski.vibits.shared.feature.mode.domain.usecase.FixInvalidOnlineModeUseCase
import space.be1ski.vibits.shared.feature.mode.domain.usecase.SaveAppModeUseCase
import space.be1ski.vibits.shared.feature.settings.domain.model.AppLanguage
import space.be1ski.vibits.shared.feature.settings.domain.model.AppTheme
import space.be1ski.vibits.shared.feature.settings.domain.usecase.LoadPreferencesUseCase

/**
 * State holder for the app root.
 * Manages app mode, theme, and language at the top level.
 */
internal class AppRootStateHolder(
  private val saveAppModeUseCase: SaveAppModeUseCase,
  private val fixInvalidOnlineModeUseCase: FixInvalidOnlineModeUseCase,
  private val loadPreferencesUseCase: LoadPreferencesUseCase,
  private val localeProvider: LocaleProvider,
) {
  private val initialPrefs = loadPreferencesUseCase()

  private val _appMode = MutableStateFlow(fixInvalidOnlineModeUseCase())
  val appMode: StateFlow<AppMode> = _appMode.asStateFlow()

  private val _appTheme = MutableStateFlow(initialPrefs.theme)
  val appTheme: StateFlow<AppTheme> = _appTheme.asStateFlow()

  private val _appLanguage = MutableStateFlow(initialPrefs.language)
  val appLanguage: StateFlow<AppLanguage> = _appLanguage.asStateFlow()

  init {
    // Configure locale on initialization
    localeProvider.configureLocale(initialPrefs.language)
  }

  fun saveAppMode(mode: AppMode) {
    saveAppModeUseCase(mode)
    _appMode.value = mode
  }

  fun updateTheme(theme: AppTheme) {
    _appTheme.value = theme
  }

  fun updateLanguage(language: AppLanguage) {
    localeProvider.configureLocale(language)
    _appLanguage.value = language
  }

  fun resetApp() {
    _appMode.value = AppMode.NOT_SELECTED
  }
}
