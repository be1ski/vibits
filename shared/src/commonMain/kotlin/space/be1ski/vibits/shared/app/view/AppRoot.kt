package space.be1ski.vibits.shared.app.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import space.be1ski.vibits.shared.app.di.AppDependencies
import space.be1ski.vibits.shared.app.di.AppGraph
import space.be1ski.vibits.shared.app.presentation.AppFeatures
import space.be1ski.vibits.shared.core.elm.Feature
import space.be1ski.vibits.shared.core.ui.theme.VibitsTheme
import space.be1ski.vibits.shared.core.ui.theme.rememberSystemDarkTheme
import space.be1ski.vibits.shared.feature.mode.di.createModeSelectionFeature
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode
import space.be1ski.vibits.shared.feature.mode.presentation.ModeSelectionAction
import space.be1ski.vibits.shared.feature.mode.presentation.ModeSelectionEffect
import space.be1ski.vibits.shared.feature.mode.presentation.ModeSelectionState
import space.be1ski.vibits.shared.feature.mode.view.ModeSelectionScreen
import space.be1ski.vibits.shared.feature.settings.domain.model.AppLanguage
import space.be1ski.vibits.shared.feature.settings.domain.model.AppTheme

@Composable
fun AppRoot(dependencies: AppDependencies) {
  val initialPrefs = remember { dependencies.loadPreferences() }
  remember { dependencies.localeProvider.configureLocale(initialPrefs.language) }

  var appMode by remember { mutableStateOf(dependencies.fixInvalidOnlineMode()) }
  var appTheme by remember { mutableStateOf(initialPrefs.theme) }
  var appLanguage by remember { mutableStateOf(initialPrefs.language) }
  var featuresVersion by remember { mutableIntStateOf(0) }
  val darkTheme = resolveDarkTheme(appTheme)
  val modeSelectionFeature =
    rememberModeSelectionFeature(dependencies) {
      featuresVersion++
      appMode = it
    }
  val features = rememberAppFeatures(featuresVersion)

  key(appLanguage) {
    VibitsTheme(darkTheme = darkTheme) {
      when (appMode) {
        AppMode.NOT_SELECTED -> ModeSelectionScreen(feature = modeSelectionFeature)
        AppMode.ONLINE, AppMode.OFFLINE, AppMode.DEMO -> {
          AppWithLoadingScreen(
            dependencies = dependencies,
            features = features,
            appTheme = appTheme,
            appLanguage = appLanguage,
            onResetApp = {
              appTheme = AppTheme.SYSTEM
              appLanguage = AppLanguage.SYSTEM
              dependencies.localeProvider.configureLocale(AppLanguage.SYSTEM)
              AppGraph.resetGraph()
              featuresVersion++
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

@Composable
private fun AppWithLoadingScreen(
  dependencies: AppDependencies,
  features: AppFeatures,
  appTheme: AppTheme,
  appLanguage: AppLanguage,
  onResetApp: () -> Unit,
  onThemeChanged: (AppTheme) -> Unit,
  onLanguageChanged: (AppLanguage) -> Unit,
) {
  val memosState by features.memos.state.collectAsState()

  if (!memosState.initialDataLoaded) {
    // Show loading screen while initial data is loading
    LoadingScreen()
  } else {
    VibitsApp(
      dependencies = dependencies,
      features = features,
      currentTheme = appTheme,
      currentLanguage = appLanguage,
      onResetApp = onResetApp,
      onThemeChanged = onThemeChanged,
      onLanguageChanged = onLanguageChanged,
    )
  }
}

@Composable
private fun LoadingScreen() {
  Box(
    modifier = Modifier.fillMaxSize(),
    contentAlignment = Alignment.Center,
  ) {
    CircularProgressIndicator()
  }
}

@Composable
private fun rememberAppFeatures(version: Int): AppFeatures =
  remember(version) {
    val factory = AppGraph.getFeaturesFactory()
    val scope = AppGraph.getAppScope()
    factory.create(scope)
  }

@Composable
private fun rememberModeSelectionFeature(
  dependencies: AppDependencies,
  onModeSelected: (AppMode) -> Unit,
): Feature<ModeSelectionAction, ModeSelectionState, ModeSelectionEffect> {
  val feature =
    remember {
      createModeSelectionFeature(dependencies = dependencies.modeSelectionDependencies)
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
