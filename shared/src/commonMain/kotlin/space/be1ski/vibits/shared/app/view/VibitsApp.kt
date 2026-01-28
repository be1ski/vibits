package space.be1ski.vibits.shared.app.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import space.be1ski.vibits.shared.app.presentation.AppFeatures
import space.be1ski.vibits.shared.feature.settings.domain.model.AppLanguage
import space.be1ski.vibits.shared.feature.settings.domain.model.AppTheme
import space.be1ski.vibits.shared.feature.settings.view.SettingsDialog

@Composable
internal fun VibitsApp(
  features: AppFeatures,
  currentTheme: AppTheme,
  currentLanguage: AppLanguage,
  onResetApp: () -> Unit = {},
  onThemeChanged: (AppTheme) -> Unit = {},
  onLanguageChanged: (AppLanguage) -> Unit = {},
) {
  val appState by features.app.state.collectAsState()
  val memosState by features.memos.state.collectAsState()
  val habitsState by features.habits.state.collectAsState()
  val settingsState by features.settings.state.collectAsState()

  FeatureCoordinator(
    features = features,
    appState = appState,
    memosState = memosState,
    settingsState = settingsState,
    currentLanguage = currentLanguage,
    currentTheme = currentTheme,
    onResetApp = onResetApp,
    onThemeChanged = onThemeChanged,
    onLanguageChanged = onLanguageChanged,
  )

  VibitsAppScaffold(
    features = features,
    appState = appState,
    memosState = memosState,
    habitsState = habitsState,
    currentLanguage = currentLanguage,
    currentTheme = currentTheme,
  )

  SettingsDialog(state = settingsState, dispatch = features.settings::send)
  MemoCreateDialog(state = memosState, dispatch = features.memos::send)
  MemoEditDialog(state = memosState, dispatch = features.memos::send)
  HabitsDialogs(appState = appState, habitsState = habitsState, dispatch = features.habits::send)
}
