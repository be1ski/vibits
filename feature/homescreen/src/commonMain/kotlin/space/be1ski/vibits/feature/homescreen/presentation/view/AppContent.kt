package space.be1ski.vibits.feature.homescreen.presentation.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import org.jetbrains.compose.resources.stringResource
import space.be1ski.vibits.core.platform.locale.AppLanguage
import space.be1ski.vibits.core.platform.mode.AppMode
import space.be1ski.vibits.core.strings.generated.Res
import space.be1ski.vibits.core.strings.generated.msg_state_loading
import space.be1ski.vibits.core.strings.generated.title_state_loading
import space.be1ski.vibits.core.ui.Indent
import space.be1ski.vibits.core.ui.StatePanel
import space.be1ski.vibits.feature.changelog.domain.usecase.GetChangelogUseCase
import space.be1ski.vibits.feature.homescreen.di.AppDependencies
import space.be1ski.vibits.feature.homescreen.presentation.AppFeatures
import space.be1ski.vibits.feature.memos.domain.repository.ExportService
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
  justFinishedOnboarding: Boolean,
  featuresState: FeaturesState,
  appTheme: AppTheme,
  appLanguage: AppLanguage,
  exportService: ExportService,
  currentVersion: String,
  getChangelog: GetChangelogUseCase,
  onResetApp: () -> Unit,
  onThemeChanged: (AppTheme) -> Unit,
  onLanguageChanged: (AppLanguage) -> Unit,
) {
  when (appMode) {
    AppMode.NOT_SELECTED -> ModeSelectionScreen(feature = featuresState.modeSelection)
    AppMode.OFFLINE ->
      if (showOnboarding) {
        val onboardingState by featuresState.onboarding.state.collectAsState()
        OnboardingScreen(
          state = onboardingState,
          onAction = featuresState.onboarding::send,
        )
      } else {
        AppWithLoadingScreen(
          features = featuresState.app,
          justFinishedOnboarding = justFinishedOnboarding,
          appTheme = appTheme,
          appLanguage = appLanguage,
          exportService = exportService,
          currentVersion = currentVersion,
          getChangelog = getChangelog,
          onResetApp = onResetApp,
          onThemeChanged = onThemeChanged,
          onLanguageChanged = onLanguageChanged,
        )
      }
    AppMode.ONLINE, AppMode.DEMO -> {
      AppWithLoadingScreen(
        features = featuresState.app,
        justFinishedOnboarding = justFinishedOnboarding,
        appTheme = appTheme,
        appLanguage = appLanguage,
        exportService = exportService,
        currentVersion = currentVersion,
        getChangelog = getChangelog,
        onResetApp = onResetApp,
        onThemeChanged = onThemeChanged,
        onLanguageChanged = onLanguageChanged,
      )
    }
  }
}

@Suppress("LongParameterList")
@Composable
private fun AppWithLoadingScreen(
  features: AppFeatures,
  justFinishedOnboarding: Boolean,
  appTheme: AppTheme,
  appLanguage: AppLanguage,
  exportService: ExportService,
  currentVersion: String,
  getChangelog: GetChangelogUseCase,
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
      justFinishedOnboarding = justFinishedOnboarding,
      currentTheme = appTheme,
      currentLanguage = appLanguage,
      exportService = exportService,
      currentVersion = currentVersion,
      getChangelog = getChangelog,
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
      modifier =
        Modifier
          .fillMaxSize()
          .padding(Indent.xl)
          .testTag(AppShellTestTags.LOADING_SCREEN),
      contentAlignment = Alignment.Center,
    ) {
      StatePanel(
        title = stringResource(Res.string.title_state_loading),
        message = stringResource(Res.string.msg_state_loading),
        icon = { CircularProgressIndicator() },
      )
    }
  }
}
