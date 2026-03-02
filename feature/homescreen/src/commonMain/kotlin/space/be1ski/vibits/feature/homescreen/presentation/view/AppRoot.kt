package space.be1ski.vibits.feature.homescreen.presentation.view

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import space.be1ski.vibits.core.platform.locale.AppLanguage
import space.be1ski.vibits.core.platform.mode.AppMode
import space.be1ski.vibits.core.ui.platform.theme.rememberSystemDarkTheme
import space.be1ski.vibits.core.ui.theme.VibitsTheme
import space.be1ski.vibits.feature.homescreen.di.AppDependencies
import space.be1ski.vibits.feature.homescreen.di.AppGraph
import space.be1ski.vibits.feature.homescreen.presentation.AppFeatures
import space.be1ski.vibits.feature.settings.domain.model.AppTheme

private val DESKTOP_BREAKPOINT = 900.dp

@Composable
fun AppRoot(
  dependencies: AppDependencies,
  onFeaturesReady: ((AppFeatures) -> Unit)? = null,
) {
  val initialPrefs = remember { dependencies.loadPreferences() }
  remember { dependencies.localeProvider.configureLocale(initialPrefs.language) }

  var appMode by remember { mutableStateOf(dependencies.fixInvalidOnlineMode()) }
  var appTheme by remember { mutableStateOf(initialPrefs.theme) }
  var appLanguage by remember { mutableStateOf(initialPrefs.language) }
  var featuresVersion by remember { mutableIntStateOf(0) }
  var showOnboarding by remember { mutableStateOf(false) }

  val darkTheme = resolveDarkTheme(appTheme)
  val featuresState =
    rememberFeaturesState(
      dependencies = dependencies,
      featuresVersion = featuresVersion,
      onModeSelected = {
        featuresVersion++
        appMode = it
      },
      onOnboardingCompleted = { showOnboarding = false },
    )

  LaunchedEffect(featuresState.app) {
    onFeaturesReady?.invoke(featuresState.app)
  }

  SyncAppMode(featuresState, appMode) { appMode = it }
  ObserveOnboardingCheck(appMode, dependencies) { showOnboarding = it }

  BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    val isDesktop = maxWidth >= DESKTOP_BREAKPOINT
    key(appLanguage) {
      VibitsTheme(darkTheme = darkTheme, isDesktop = isDesktop) {
        AppContent(
          appMode = appMode,
          showOnboarding = showOnboarding,
          featuresState = featuresState,
          appTheme = appTheme,
          appLanguage = appLanguage,
          exportService = dependencies.settingsDependencies.exportService,
          onResetApp = {
            resetApp(
              dependencies = dependencies,
              onThemeReset = { appTheme = AppTheme.SYSTEM },
              onLanguageReset = { appLanguage = AppLanguage.SYSTEM },
              onVersionIncrement = { featuresVersion++ },
              onModeReset = { appMode = AppMode.NOT_SELECTED },
              onOnboardingReset = { showOnboarding = false },
            )
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

@Composable
private fun resolveDarkTheme(theme: AppTheme): Boolean {
  val systemDarkTheme = rememberSystemDarkTheme()
  return when (theme) {
    AppTheme.SYSTEM -> systemDarkTheme
    AppTheme.LIGHT -> false
    AppTheme.DARK -> true
  }
}

@Suppress("LongParameterList")
private fun resetApp(
  dependencies: AppDependencies,
  onThemeReset: () -> Unit,
  onLanguageReset: () -> Unit,
  onVersionIncrement: () -> Unit,
  onModeReset: () -> Unit,
  onOnboardingReset: () -> Unit,
) {
  onThemeReset()
  onLanguageReset()
  dependencies.localeProvider.configureLocale(AppLanguage.SYSTEM)
  AppGraph.resetGraph()
  onVersionIncrement()
  onModeReset()
  onOnboardingReset()
}
