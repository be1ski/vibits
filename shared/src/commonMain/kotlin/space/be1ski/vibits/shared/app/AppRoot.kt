package space.be1ski.vibits.shared.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import space.be1ski.vibits.shared.core.elm.Feature
import space.be1ski.vibits.shared.core.platform.ProvideDateFormatter
import space.be1ski.vibits.shared.core.ui.theme.VibitsTheme
import space.be1ski.vibits.shared.core.ui.theme.rememberSystemDarkTheme
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode
import space.be1ski.vibits.shared.feature.mode.presentation.ModeSelectionAction
import space.be1ski.vibits.shared.feature.mode.presentation.ModeSelectionEffect
import space.be1ski.vibits.shared.feature.mode.presentation.ModeSelectionScreen
import space.be1ski.vibits.shared.feature.mode.presentation.ModeSelectionState
import space.be1ski.vibits.shared.feature.mode.presentation.createModeSelectionFeature
import space.be1ski.vibits.shared.feature.settings.domain.model.AppLanguage
import space.be1ski.vibits.shared.feature.settings.domain.model.AppTheme

@Composable
fun AppRoot(dependencies: AppDependencies) {
  val initialPrefs = remember { dependencies.loadPreferences() }
  remember { dependencies.localeProvider.configureLocale(initialPrefs.language) }

  var appMode by remember { mutableStateOf(dependencies.fixInvalidOnlineMode()) }
  var appTheme by remember { mutableStateOf(initialPrefs.theme) }
  var appLanguage by remember { mutableStateOf(initialPrefs.language) }
  val darkTheme = resolveDarkTheme(appTheme)
  val modeSelectionFeature = rememberModeSelectionFeature(dependencies) { appMode = it }

  key(appLanguage) {
    ProvideDateFormatter {
      VibitsTheme(darkTheme = darkTheme) {
        when (appMode) {
          AppMode.NOT_SELECTED -> ModeSelectionScreen(feature = modeSelectionFeature)
          AppMode.ONLINE, AppMode.OFFLINE, AppMode.DEMO -> {
            VibitsApp(
              dependencies = dependencies.vibitsApp,
              currentTheme = appTheme,
              currentLanguage = appLanguage,
              onResetApp = {
                appTheme = AppTheme.SYSTEM
                appLanguage = AppLanguage.SYSTEM
                dependencies.localeProvider.configureLocale(AppLanguage.SYSTEM)
                appMode = AppMode.NOT_SELECTED
              },
              onThemeChanged = { appTheme = it },
              onLanguageChanged = {
                dependencies.localeProvider.configureLocale(it)
                appLanguage = it
              },
            )
          }
        }
      }
    }
  }
}

@Composable
private fun rememberModeSelectionFeature(
  dependencies: AppDependencies,
  onModeSelected: (AppMode) -> Unit,
): Feature<ModeSelectionAction, ModeSelectionState, ModeSelectionEffect> {
  val feature =
    remember {
      createModeSelectionFeature(
        validateCredentials = dependencies.modeSelection.validateCredentials,
        saveCredentials = dependencies.modeSelection.saveCredentials,
        saveAppMode = dependencies.modeSelection.saveAppMode,
      )
    }
  val scope = rememberCoroutineScope()
  LaunchedEffect(feature) { feature.launchIn(scope) }
  LaunchedEffect(feature) {
    feature.effects.collect { effect ->
      if (effect is ModeSelectionEffect.NotifyModeSelected) onModeSelected(effect.mode)
    }
  }
  return feature
}

@Composable
private fun resolveDarkTheme(theme: AppTheme): Boolean {
  val systemDarkTheme = rememberSystemDarkTheme()
  return when (theme) {
    AppTheme.SYSTEM -> systemDarkTheme
    AppTheme.LIGHT -> false
    AppTheme.DARK -> true
  }
}
