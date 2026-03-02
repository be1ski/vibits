package space.be1ski.vibits.feature.homescreen.presentation.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import space.be1ski.vibits.core.platform.locale.AppLanguage
import space.be1ski.vibits.core.ui.theme.LocalWideLayout
import space.be1ski.vibits.feature.habits.presentation.action.HabitsAction
import space.be1ski.vibits.feature.homescreen.domain.model.AppState
import space.be1ski.vibits.feature.homescreen.domain.model.Screen
import space.be1ski.vibits.feature.homescreen.presentation.AppFeatures
import space.be1ski.vibits.feature.homescreen.presentation.action.AppAction
import space.be1ski.vibits.feature.memos.presentation.action.MemosAction
import space.be1ski.vibits.feature.memos.presentation.state.MemosState
import space.be1ski.vibits.feature.settings.domain.model.AppTheme
import space.be1ski.vibits.feature.settings.presentation.action.SettingsAction
import space.be1ski.vibits.feature.settings.presentation.effect.SettingsEffect
import space.be1ski.vibits.feature.settings.presentation.state.SettingsState

@Composable
internal fun FeatureCoordinator(
  features: AppFeatures,
  appState: AppState,
  memosState: MemosState,
  settingsState: SettingsState,
  currentLanguage: AppLanguage,
  currentTheme: AppTheme,
  onResetApp: () -> Unit,
  onThemeChanged: (AppTheme) -> Unit,
  onLanguageChanged: (AppLanguage) -> Unit,
) {
  val dispatchApp = features.app::send
  val dispatchMemos = features.memos::send
  val dispatchHabits = features.habits::send

  // Cross-feature coordination via Settings notifications
  LaunchedEffect(features.settings) {
    features.settings.notifications.collect { notification ->
      val currentMemosState = features.memos.state.value
      handleNotification(
        notification,
        currentMemosState.baseUrl,
        currentMemosState.token,
        dispatchApp,
        dispatchMemos,
        dispatchHabits,
        onResetApp,
        onThemeChanged,
        onLanguageChanged,
      )
    }
  }

  // Auto-open settings when credentials required (skip for DEMO/OFFLINE modes)
  val wideLayout = LocalWideLayout.current
  LaunchedEffect(memosState.credentialsMode, settingsState.isOpen, appState.appMode) {
    val skipCredentialsCheck = appState.skipCredentialsCheck
    if (!skipCredentialsCheck && memosState.credentialsMode && !settingsState.isOpen) {
      if (wideLayout) {
        features.app.send(AppAction.Navigation.SelectScreen(Screen.SETTINGS))
      }
      features.settings.send(
        SettingsAction.Dialog.Open(
          baseUrl = memosState.baseUrl,
          token = memosState.token,
          appMode = appState.appMode,
          language = currentLanguage,
          theme = currentTheme,
          syncDebounceSeconds = settingsState.selectedSyncDebounceSeconds,
        ),
      )
    }
  }

  // Auto-load memos on app start
  LaunchedEffect(memosState.credentialsMode, appState.autoLoaded, memosState.isLoading, appState.appMode) {
    val skipCredentialsCheck = appState.skipCredentialsCheck
    val shouldAutoLoad =
      !memosState.credentialsMode &&
        !appState.autoLoaded &&
        !memosState.isLoading &&
        (skipCredentialsCheck || memosState.hasCredentials)
    if (shouldAutoLoad) {
      dispatchApp(AppAction.UI.MarkAutoLoaded)
      dispatchMemos(MemosAction.Loading.LoadMemos)
    }
  }
}

@Suppress("LongParameterList")
private fun handleNotification(
  effect: SettingsEffect.Notification,
  currentMemosBaseUrl: String,
  currentMemosToken: String,
  dispatchApp: (AppAction) -> Unit,
  dispatchMemos: (MemosAction) -> Unit,
  dispatchHabits: (HabitsAction) -> Unit,
  onResetApp: () -> Unit,
  onThemeChanged: (AppTheme) -> Unit,
  onLanguageChanged: (AppLanguage) -> Unit,
) {
  when (effect) {
    is SettingsEffect.Notification.ModeChanged -> {
      dispatchApp(AppAction.Mode.SetAppMode(effect.newMode))
      dispatchMemos(MemosAction.Loading.ResetForModeChange(effect.newMode))
      dispatchMemos(MemosAction.Loading.LoadMemos)
      dispatchHabits(HabitsAction.Cache.InvalidateAllCache)
    }
    is SettingsEffect.Notification.ResetCompleted -> onResetApp()
    is SettingsEffect.Notification.CredentialsSaved -> {
      val credentialsChanged = effect.baseUrl != currentMemosBaseUrl || effect.token != currentMemosToken
      dispatchMemos(MemosAction.Credentials.UpdateBaseUrl(effect.baseUrl))
      dispatchMemos(MemosAction.Credentials.UpdateToken(effect.token))
      if (credentialsChanged) {
        dispatchMemos(MemosAction.Loading.LoadMemos)
      }
    }
    is SettingsEffect.Notification.ThemeChanged -> onThemeChanged(effect.theme)
    is SettingsEffect.Notification.LanguageChanged -> onLanguageChanged(effect.language)
    is SettingsEffect.Notification.DialogClosed -> Unit
  }
}
