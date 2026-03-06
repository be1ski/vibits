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
  val rootState = rememberAppRootState(dependencies)
  val darkTheme = resolveDarkTheme(rootState.appTheme)
  val featuresState =
    rememberFeaturesState(
      dependencies = dependencies,
      featuresVersion = rootState.featuresVersion,
      onModeSelected = {
        rootState.featuresVersion++
        rootState.appMode = it
      },
      onOnboardingCompleted = {
        rootState.showOnboarding = false
        rootState.justFinishedOnboarding = true
      },
    )

  LaunchedEffect(featuresState.app) {
    onFeaturesReady?.invoke(featuresState.app)
  }

  SyncAppMode(featuresState, rootState.appMode) { rootState.appMode = it }
  ObserveOnboardingCheck(rootState.appMode, dependencies) { rootState.showOnboarding = it }

  BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    val wideLayout = maxWidth >= DESKTOP_BREAKPOINT
    key(rootState.appLanguage) {
      VibitsTheme(darkTheme = darkTheme, wideLayout = wideLayout) {
        AppContent(
          appMode = rootState.appMode,
          showOnboarding = rootState.showOnboarding,
          justFinishedOnboarding = rootState.justFinishedOnboarding,
          featuresState = featuresState,
          config = rootState.buildConfig(dependencies),
          callbacks = rootState.buildCallbacks(dependencies),
        )
      }
    }
  }
}

private class AppRootState(
  initialMode: AppMode,
  initialTheme: AppTheme,
  initialLanguage: AppLanguage,
  val currentVersion: String,
) {
  var appMode by mutableStateOf(initialMode)
  var appTheme by mutableStateOf(initialTheme)
  var appLanguage by mutableStateOf(initialLanguage)
  var featuresVersion by mutableIntStateOf(0)
  var showOnboarding by mutableStateOf(false)
  var justFinishedOnboarding by mutableStateOf(false)

  fun buildConfig(dependencies: AppDependencies) =
    AppContentConfig(
      appTheme = appTheme,
      appLanguage = appLanguage,
      exportService = dependencies.settingsDependencies.exportService,
      currentVersion = currentVersion,
      getChangelog = dependencies.getChangelog,
      checkForUpdate = dependencies.checkForUpdate,
      appUpdater = dependencies.appUpdater,
    )

  fun buildCallbacks(dependencies: AppDependencies) =
    AppContentCallbacks(
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

@Composable
private fun rememberAppRootState(dependencies: AppDependencies): AppRootState {
  val initialPrefs = remember { dependencies.loadPreferences() }
  val currentVersion = remember { dependencies.loadAppDetails().version }
  remember { dependencies.localeProvider.configureLocale(initialPrefs.language) }
  return remember {
    AppRootState(
      initialMode = dependencies.fixInvalidOnlineMode(),
      initialTheme = initialPrefs.theme,
      initialLanguage = initialPrefs.language,
      currentVersion = currentVersion,
    )
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
