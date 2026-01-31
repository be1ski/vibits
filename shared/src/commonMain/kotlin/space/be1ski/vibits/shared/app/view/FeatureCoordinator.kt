package space.be1ski.vibits.shared.app.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import space.be1ski.vibits.shared.app.domain.model.AppState
import space.be1ski.vibits.shared.app.presentation.AppFeatures
import space.be1ski.vibits.shared.app.presentation.action.AppAction
import space.be1ski.vibits.shared.feature.habits.presentation.action.HabitsAction
import space.be1ski.vibits.shared.feature.memos.presentation.action.MemosAction
import space.be1ski.vibits.shared.feature.memos.presentation.state.MemosState
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode
import space.be1ski.vibits.shared.feature.settings.domain.model.AppLanguage
import space.be1ski.vibits.shared.feature.settings.domain.model.AppTheme
import space.be1ski.vibits.shared.feature.settings.presentation.action.SettingsAction
import space.be1ski.vibits.shared.feature.settings.presentation.effect.SettingsEffect
import space.be1ski.vibits.shared.feature.settings.presentation.state.SettingsState

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
      handleNotification(notification, dispatchApp, dispatchMemos, dispatchHabits, onResetApp, onThemeChanged, onLanguageChanged)
    }
  }

  // Auto-open settings when credentials required (skip for DEMO/OFFLINE modes)
  LaunchedEffect(memosState.credentialsMode, settingsState.isOpen, appState.appMode) {
    val skipCredentialsCheck = appState.skipCredentialsCheck
    if (!skipCredentialsCheck && memosState.credentialsMode && !settingsState.isOpen) {
      features.settings.send(
        SettingsAction.Dialog.Open(
          baseUrl = memosState.baseUrl,
          token = memosState.token,
          appMode = appState.appMode,
          language = currentLanguage,
          theme = currentTheme,
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
      dispatchApp(AppAction.MarkAutoLoaded)
      dispatchMemos(MemosAction.Loading.LoadMemos)
    }
  }
}

@Suppress("LongParameterList")
private fun handleNotification(
  effect: SettingsEffect.Notification,
  dispatchApp: (AppAction) -> Unit,
  dispatchMemos: (MemosAction) -> Unit,
  dispatchHabits: (HabitsAction) -> Unit,
  onResetApp: () -> Unit,
  onThemeChanged: (AppTheme) -> Unit,
  onLanguageChanged: (AppLanguage) -> Unit,
) {
  when (effect) {
    is SettingsEffect.Notification.ModeChanged -> {
      dispatchApp(AppAction.SetAppMode(effect.newMode))
      dispatchMemos(MemosAction.Loading.ResetForModeChange(effect.newMode))
      dispatchMemos(MemosAction.Loading.LoadMemos)
      dispatchHabits(HabitsAction.Cache.InvalidateAllCache)
    }
    is SettingsEffect.Notification.ResetCompleted -> onResetApp()
    is SettingsEffect.Notification.CredentialsSaved -> {
      dispatchMemos(MemosAction.Credentials.UpdateBaseUrl(effect.baseUrl))
      dispatchMemos(MemosAction.Credentials.UpdateToken(effect.token))
      dispatchMemos(MemosAction.Loading.LoadMemos)
    }
    is SettingsEffect.Notification.ThemeChanged -> onThemeChanged(effect.theme)
    is SettingsEffect.Notification.LanguageChanged -> onLanguageChanged(effect.language)
    is SettingsEffect.Notification.DialogClosed -> Unit
  }
}
