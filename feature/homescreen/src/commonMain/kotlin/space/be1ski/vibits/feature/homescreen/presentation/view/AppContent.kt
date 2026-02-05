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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import space.be1ski.vibits.core.platform.locale.AppLanguage
import space.be1ski.vibits.core.platform.mode.AppMode
import space.be1ski.vibits.feature.homescreen.di.AppDependencies
import space.be1ski.vibits.feature.homescreen.presentation.AppFeatures
import space.be1ski.vibits.feature.mode.presentation.view.ModeSelectionScreen
import space.be1ski.vibits.feature.onboarding.presentation.view.OnboardingScreen
import space.be1ski.vibits.feature.settings.domain.model.AppTheme

@Composable
internal fun SyncAppMode(
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
internal fun ObserveOnboardingCheck(
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

@Suppress("LongParameterList")
@Composable
internal fun AppContent(
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
