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
  callbacks: AppContentCallbacks,
) {
  val dispatchers =
    FeatureDispatchers(
      dispatchApp = features.app::send,
      dispatchMemos = features.memos::send,
      dispatchHabits = features.habits::send,
    )

  // Cross-feature coordination via Settings notifications
  LaunchedEffect(features.settings) {
    features.settings.notifications.collect { notification ->
      val currentMemosState = features.memos.state.value
      handleNotification(
        notification,
        currentMemosState.baseUrl,
        currentMemosState.token,
        dispatchers,
        callbacks,
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
      dispatchers.dispatchApp(AppAction.UI.MarkAutoLoaded)
      dispatchers.dispatchMemos(MemosAction.Loading.LoadMemos)
    }
  }
}

private fun handleNotification(
  effect: SettingsEffect.Notification,
  currentMemosBaseUrl: String,
  currentMemosToken: String,
  dispatchers: FeatureDispatchers,
  callbacks: AppContentCallbacks,
) {
  when (effect) {
    is SettingsEffect.Notification.ModeChanged -> {
      dispatchers.dispatchApp(AppAction.Mode.SetAppMode(effect.newMode))
      dispatchers.dispatchMemos(MemosAction.Loading.ResetForModeChange(effect.newMode))
      dispatchers.dispatchMemos(MemosAction.Loading.LoadMemos)
      dispatchers.dispatchHabits(HabitsAction.Cache.InvalidateAllCache)
    }
    is SettingsEffect.Notification.ResetCompleted -> callbacks.onResetApp()
    is SettingsEffect.Notification.CredentialsSaved -> {
      val credentialsChanged = effect.baseUrl != currentMemosBaseUrl || effect.token != currentMemosToken
      dispatchers.dispatchMemos(MemosAction.Credentials.UpdateBaseUrl(effect.baseUrl))
      dispatchers.dispatchMemos(MemosAction.Credentials.UpdateToken(effect.token))
      if (credentialsChanged) {
        dispatchers.dispatchMemos(MemosAction.Loading.LoadMemos)
      }
    }
    is SettingsEffect.Notification.ThemeChanged -> callbacks.onThemeChanged(effect.theme)
    is SettingsEffect.Notification.LanguageChanged -> callbacks.onLanguageChanged(effect.language)
    is SettingsEffect.Notification.DialogClosed -> Unit
  }
}
