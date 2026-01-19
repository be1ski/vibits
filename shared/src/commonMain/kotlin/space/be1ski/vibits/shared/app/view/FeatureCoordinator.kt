package space.be1ski.vibits.shared.app.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import space.be1ski.vibits.shared.feature.memos.presentation.MemosAction
import space.be1ski.vibits.shared.feature.memos.presentation.MemosState
import space.be1ski.vibits.shared.feature.settings.domain.model.AppLanguage
import space.be1ski.vibits.shared.feature.settings.domain.model.AppTheme
import space.be1ski.vibits.shared.feature.settings.presentation.SettingsAction
import space.be1ski.vibits.shared.feature.settings.presentation.SettingsEffect
import space.be1ski.vibits.shared.feature.settings.presentation.SettingsState

@Composable
internal fun FeatureCoordinator(
  features: AppFeatures,
  memosState: MemosState,
  settingsState: SettingsState,
  currentLanguage: AppLanguage,
  currentTheme: AppTheme,
  onResetApp: () -> Unit,
  onThemeChanged: (AppTheme) -> Unit,
  onLanguageChanged: (AppLanguage) -> Unit,
) {
  val dispatchMemos = features.memos::send

  // Handle settings effects
  LaunchedEffect(features.settings) {
    features.settings.effects.collect { effect ->
      when (effect) {
        is SettingsEffect.NotifyModeChanged -> {
          features.appState.appMode = effect.newMode
          dispatchMemos(MemosAction.LoadMemos)
        }
        is SettingsEffect.NotifyResetCompleted -> onResetApp()
        is SettingsEffect.NotifyCredentialsSaved -> {
          dispatchMemos(MemosAction.UpdateBaseUrl(effect.baseUrl))
          dispatchMemos(MemosAction.UpdateToken(effect.token))
          dispatchMemos(MemosAction.LoadMemos)
        }
        is SettingsEffect.NotifyThemeChanged -> onThemeChanged(effect.theme)
        is SettingsEffect.NotifyLanguageChanged -> onLanguageChanged(effect.language)
        is SettingsEffect.NotifyDialogClosed -> Unit
        else -> Unit
      }
    }
  }

  // Auto-open settings when credentials required
  LaunchedEffect(memosState.credentialsMode, settingsState.isOpen) {
    if (memosState.credentialsMode && !settingsState.isOpen) {
      features.settings.send(
        SettingsAction.Open(
          baseUrl = memosState.baseUrl,
          token = memosState.token,
          appMode = features.appState.appMode,
          language = currentLanguage,
          theme = currentTheme,
        ),
      )
    }
  }

  SyncAutoLoad(memosState, features.appState, dispatchMemos)
}
