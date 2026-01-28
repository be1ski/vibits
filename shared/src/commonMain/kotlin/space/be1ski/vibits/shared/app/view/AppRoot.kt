@file:Suppress("TooManyFunctions")

package space.be1ski.vibits.shared.app.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import space.be1ski.vibits.shared.feature.onboarding.di.createOnboardingFeature
import space.be1ski.vibits.shared.feature.onboarding.presentation.OnboardingAction
import space.be1ski.vibits.shared.feature.onboarding.presentation.OnboardingEffect
import space.be1ski.vibits.shared.feature.onboarding.presentation.OnboardingState
import space.be1ski.vibits.shared.feature.onboarding.view.OnboardingScreen
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
  var showOnboarding by remember { mutableStateOf(false) }

  // Check if onboarding should be shown for Offline mode
  LaunchedEffect(appMode) {
    if (appMode == AppMode.OFFLINE) {
      showOnboarding = dependencies.shouldShowOnboarding()
    } else {
      showOnboarding = false
    }
  }

  val darkTheme = resolveDarkTheme(appTheme)
  val featuresState =
    rememberFeaturesState(
      dependencies = dependencies,
      featuresVersion = featuresVersion,
      appMode = appMode,
      onModeSelected = {
        featuresVersion++
        appMode = it
      },
      onOnboardingCompleted = { showOnboarding = false },
      onShowOnboarding = { showOnboarding = it },
    )

  key(appLanguage) {
    VibitsTheme(darkTheme = darkTheme) {
      AppContent(
        appMode = appMode,
        showOnboarding = showOnboarding,
        featuresState = featuresState,
        appTheme = appTheme,
        appLanguage = appLanguage,
        onResetApp = {
          createResetAppCallback(
            dependencies = dependencies,
            onThemeReset = { appTheme = AppTheme.SYSTEM },
            onLanguageReset = { appLanguage = AppLanguage.SYSTEM },
            onVersionIncrement = { featuresVersion++ },
            onModeReset = { appMode = AppMode.NOT_SELECTED },
            onOnboardingReset = { showOnboarding = false },
          )()
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

@Composable
private fun AppWithLoadingScreen(
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
  Surface(
    modifier = Modifier.fillMaxSize(),
    color = MaterialTheme.colorScheme.background,
  ) {
    Box(
      modifier = Modifier.fillMaxSize(),
      contentAlignment = Alignment.Center,
    ) {
      CircularProgressIndicator()
    }
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
private fun rememberOnboardingFeature(
  dependencies: AppDependencies,
  onOnboardingCompleted: () -> Unit,
): Feature<OnboardingAction, OnboardingState, OnboardingEffect> {
  val feature =
    remember {
      createOnboardingFeature(dependencies = dependencies.onboardingDependencies)
    }
  val scope = rememberCoroutineScope()
  LaunchedEffect(feature) { feature.launchIn(scope) }
  LaunchedEffect(feature) {
    feature.effects.collect { effect ->
      if (effect is OnboardingEffect.Notification.Completed || effect is OnboardingEffect.Notification.Skipped) {
        onOnboardingCompleted()
      }
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

@Composable
private fun ObserveOnboardingTrigger(
  appMode: AppMode,
  features: AppFeatures,
  dependencies: AppDependencies,
  onShowOnboarding: (Boolean) -> Unit,
) {
  if (appMode != AppMode.NOT_SELECTED) {
    val appState by features.app.state.collectAsState()
    LaunchedEffect(appState.appMode) {
      if (appState.appMode == AppMode.OFFLINE && appMode == AppMode.OFFLINE) {
        val shouldShow = dependencies.shouldShowOnboarding()
        if (shouldShow) {
          onShowOnboarding(true)
        }
      }
    }
  }
}

private data class FeaturesState(
  val modeSelection: Feature<ModeSelectionAction, ModeSelectionState, ModeSelectionEffect>,
  val onboarding: Feature<OnboardingAction, OnboardingState, OnboardingEffect>,
  val app: AppFeatures,
)

@Suppress("LongParameterList")
@Composable
private fun rememberFeaturesState(
  dependencies: AppDependencies,
  featuresVersion: Int,
  appMode: AppMode,
  onModeSelected: (AppMode) -> Unit,
  onOnboardingCompleted: () -> Unit,
  onShowOnboarding: (Boolean) -> Unit,
): FeaturesState {
  val modeSelectionFeature =
    key(featuresVersion) {
      rememberModeSelectionFeature(dependencies, onModeSelected)
    }
  val onboardingFeature =
    key(featuresVersion) {
      rememberOnboardingFeature(dependencies, onOnboardingCompleted)
    }
  val appFeatures = rememberAppFeatures(featuresVersion)

  ObserveOnboardingTrigger(
    appMode = appMode,
    features = appFeatures,
    dependencies = dependencies,
    onShowOnboarding = onShowOnboarding,
  )

  return FeaturesState(modeSelectionFeature, onboardingFeature, appFeatures)
}

@Suppress("LongParameterList")
@Composable
private fun AppContent(
  appMode: AppMode,
  showOnboarding: Boolean,
  featuresState: FeaturesState,
  appTheme: AppTheme,
  appLanguage: AppLanguage,
  onResetApp: () -> Unit,
  onThemeChanged: (AppTheme) -> Unit,
  onLanguageChanged: (AppLanguage) -> Unit,
) {
  when {
    appMode == AppMode.NOT_SELECTED -> ModeSelectionScreen(feature = featuresState.modeSelection)
    appMode == AppMode.OFFLINE && showOnboarding -> {
      val onboardingState by featuresState.onboarding.state.collectAsState()
      OnboardingScreen(
        state = onboardingState,
        onAction = featuresState.onboarding::send,
      )
    }
    appMode == AppMode.ONLINE || appMode == AppMode.OFFLINE || appMode == AppMode.DEMO -> {
      AppWithLoadingScreen(
        features = featuresState.app,
        appTheme = appTheme,
        appLanguage = appLanguage,
        onResetApp = onResetApp,
        onThemeChanged = onThemeChanged,
        onLanguageChanged = onLanguageChanged,
      )
    }
  }
}

@Suppress("LongParameterList")
private fun createResetAppCallback(
  dependencies: AppDependencies,
  onThemeReset: () -> Unit,
  onLanguageReset: () -> Unit,
  onVersionIncrement: () -> Unit,
  onModeReset: () -> Unit,
  onOnboardingReset: () -> Unit,
): () -> Unit =
  {
    onThemeReset()
    onLanguageReset()
    dependencies.localeProvider.configureLocale(AppLanguage.SYSTEM)
    AppGraph.resetGraph()
    onVersionIncrement()
    onModeReset()
    onOnboardingReset()
  }
