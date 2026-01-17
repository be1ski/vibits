package space.be1ski.vibits.shared.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import org.koin.compose.koinInject
import space.be1ski.vibits.shared.core.platform.LocaleProvider
import space.be1ski.vibits.shared.core.ui.theme.VibitsTheme
import space.be1ski.vibits.shared.core.ui.theme.rememberSystemDarkTheme
import space.be1ski.vibits.shared.feature.auth.domain.usecase.SaveCredentialsUseCase
import space.be1ski.vibits.shared.feature.auth.domain.usecase.ValidateCredentialsUseCase
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode
import space.be1ski.vibits.shared.feature.mode.domain.usecase.FixInvalidOnlineModeUseCase
import space.be1ski.vibits.shared.feature.mode.domain.usecase.SaveAppModeUseCase
import space.be1ski.vibits.shared.feature.mode.presentation.ModeSelectionEffect
import space.be1ski.vibits.shared.feature.mode.presentation.ModeSelectionScreen
import space.be1ski.vibits.shared.feature.mode.presentation.createModeSelectionFeature
import space.be1ski.vibits.shared.feature.settings.domain.model.AppTheme
import space.be1ski.vibits.shared.feature.settings.domain.usecase.LoadPreferencesUseCase

@Composable
fun AppRoot() {
  val fixInvalidOnlineModeUseCase: FixInvalidOnlineModeUseCase = koinInject()
  val loadPreferencesUseCase: LoadPreferencesUseCase = koinInject()
  val localeProvider: LocaleProvider = koinInject()
  val validateCredentialsUseCase: ValidateCredentialsUseCase = koinInject()
  val saveCredentialsUseCase: SaveCredentialsUseCase = koinInject()
  val saveAppModeUseCase: SaveAppModeUseCase = koinInject()

  val initialPrefs = remember { loadPreferencesUseCase() }
  remember { localeProvider.configureLocale(initialPrefs.language) }

  var appMode by remember { mutableStateOf(fixInvalidOnlineModeUseCase()) }
  var appTheme by remember { mutableStateOf(initialPrefs.theme) }
  var appLanguage by remember { mutableStateOf(initialPrefs.language) }

  val systemDarkTheme = rememberSystemDarkTheme()
  val darkTheme =
    when (appTheme) {
      AppTheme.SYSTEM -> systemDarkTheme
      AppTheme.LIGHT -> false
      AppTheme.DARK -> true
    }

  // ModeSelectionFeature
  val modeSelectionFeature =
    remember {
      createModeSelectionFeature(
        validateCredentials = validateCredentialsUseCase,
        saveCredentials = saveCredentialsUseCase,
        saveAppMode = saveAppModeUseCase,
      )
    }
  val scope = rememberCoroutineScope()
  LaunchedEffect(modeSelectionFeature) {
    modeSelectionFeature.launchIn(scope)
  }

  // Observe mode selection effects
  LaunchedEffect(modeSelectionFeature) {
    modeSelectionFeature.effects.collect { effect ->
      when (effect) {
        is ModeSelectionEffect.NotifyModeSelected -> {
          appMode = effect.mode
        }
        else -> {
          // Internal effects handled by EffectHandler
        }
      }
    }
  }

  // Use key to force full recomposition when language changes
  key(appLanguage) {
    VibitsTheme(darkTheme = darkTheme) {
      when (appMode) {
        AppMode.NOT_SELECTED -> {
          ModeSelectionScreen(feature = modeSelectionFeature)
        }
        AppMode.ONLINE, AppMode.OFFLINE, AppMode.DEMO -> {
          VibitsApp(
            currentTheme = appTheme,
            currentLanguage = appLanguage,
            onResetApp = { appMode = AppMode.NOT_SELECTED },
            onThemeChanged = { theme -> appTheme = theme },
            onLanguageChanged = { language ->
              localeProvider.configureLocale(language)
              appLanguage = language
            },
          )
        }
      }
    }
  }
}
