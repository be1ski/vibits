@file:Suppress("TooManyFunctions")

package space.be1ski.vibits.feature.homescreen.presentation.view

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
import space.be1ski.vibits.core.elm.Feature
import space.be1ski.vibits.core.platform.locale.AppLanguage
import space.be1ski.vibits.core.platform.mode.AppMode
import space.be1ski.vibits.core.ui.theme.VibitsTheme
import space.be1ski.vibits.core.ui.theme.rememberSystemDarkTheme
import space.be1ski.vibits.feature.homescreen.di.AppDependencies
import space.be1ski.vibits.feature.homescreen.di.AppGraph
import space.be1ski.vibits.feature.homescreen.presentation.AppFeatures
import space.be1ski.vibits.feature.memos.presentation.action.MemosAction
import space.be1ski.vibits.feature.mode.di.createModeSelectionFeature
import space.be1ski.vibits.feature.mode.presentation.action.ModeSelectionAction
import space.be1ski.vibits.feature.mode.presentation.effect.ModeSelectionEffect
import space.be1ski.vibits.feature.mode.presentation.state.ModeSelectionState
import space.be1ski.vibits.feature.mode.presentation.view.ModeSelectionScreen
import space.be1ski.vibits.feature.onboarding.di.createOnboardingFeature
import space.be1ski.vibits.feature.onboarding.presentation.action.OnboardingAction
import space.be1ski.vibits.feature.onboarding.presentation.effect.OnboardingEffect
import space.be1ski.vibits.feature.onboarding.presentation.state.OnboardingState
import space.be1ski.vibits.feature.onboarding.presentation.view.OnboardingScreen
import space.be1ski.vibits.feature.settings.domain.model.AppTheme

@Composable
fun AppRoot(dependencies: AppDependencies) {
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

  SyncAppMode(featuresState, appMode) { appMode = it }
  ObserveOnboardingCheck(appMode, dependencies) { showOnboarding = it }

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
private fun SyncAppMode(
  featuresState: FeaturesState,
  currentAppMode: AppMode,
  onModeUpdated: (AppMode) -> Unit,
) {
  val appState by featuresState.app.app.state
    .collectAsState()
  LaunchedEffect(appState.appMode) {
    if (appState.appMode != AppMode.NOT_SELECTED && appState.appMode != currentAppMode) {
      onModeUpdated(appState.appMode)
    }
  }
}

@Composable
private fun ObserveOnboardingCheck(
  appMode: AppMode,
  dependencies: AppDependencies,
  onShowOnboarding: (Boolean) -> Unit,
) {
  LaunchedEffect(appMode) {
    if (appMode == AppMode.OFFLINE) {
      onShowOnboarding(dependencies.shouldShowOnboarding())
    } else {
      onShowOnboarding(false)
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
): Feature<ModeSelectionAction, ModeSelectionState, ModeSelectionEffect.Command, ModeSelectionEffect.Notification> {
  val feature =
    remember {
      createModeSelectionFeature(dependencies = dependencies.modeSelectionDependencies)
    }
  val scope = rememberCoroutineScope()
  LaunchedEffect(feature) { feature.launchIn(scope) }
  LaunchedEffect(feature) {
    feature.notifications.collect { notification ->
      when (notification) {
        is ModeSelectionEffect.Notification.ModeSelected -> onModeSelected(notification.mode)
      }
    }
  }
  return feature
}

@Composable
private fun rememberOnboardingFeature(
  dependencies: AppDependencies,
  features: AppFeatures,
  onOnboardingCompleted: () -> Unit,
): Feature<OnboardingAction, OnboardingState, OnboardingEffect.Command, OnboardingEffect.Notification> {
  val feature =
    remember {
      createOnboardingFeature(dependencies = dependencies.onboardingDependencies)
    }
  val scope = rememberCoroutineScope()
  LaunchedEffect(feature) { feature.launchIn(scope) }
  LaunchedEffect(feature) {
    feature.notifications.collect { notification ->
      when (notification) {
        is OnboardingEffect.Notification.Completed,
        is OnboardingEffect.Notification.Skipped,
        -> onOnboardingCompleted()
        is OnboardingEffect.Notification.FirstCheckInCreated -> {
          features.memos.send(MemosAction.Loading.LoadMemos)
        }
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

private data class FeaturesState(
  val modeSelection: Feature<ModeSelectionAction, ModeSelectionState, ModeSelectionEffect.Command, ModeSelectionEffect.Notification>,
  val onboarding: Feature<OnboardingAction, OnboardingState, OnboardingEffect.Command, OnboardingEffect.Notification>,
  val app: AppFeatures,
)

@Composable
private fun rememberFeaturesState(
  dependencies: AppDependencies,
  featuresVersion: Int,
  onModeSelected: (AppMode) -> Unit,
  onOnboardingCompleted: () -> Unit,
): FeaturesState {
  val modeSelectionFeature =
    key(featuresVersion) {
      rememberModeSelectionFeature(dependencies, onModeSelected)
    }
  val appFeatures = rememberAppFeatures(featuresVersion)
  val onboardingFeature =
    key(featuresVersion) {
      rememberOnboardingFeature(dependencies, appFeatures, onOnboardingCompleted)
    }

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
