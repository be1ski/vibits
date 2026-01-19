package space.be1ski.vibits.shared.app.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import space.be1ski.vibits.shared.app.di.AppDependencies
import space.be1ski.vibits.shared.feature.settings.domain.model.AppLanguage
import space.be1ski.vibits.shared.feature.settings.domain.model.AppTheme
import space.be1ski.vibits.shared.feature.settings.view.SettingsDialog

@Composable
fun VibitsApp(
  dependencies: AppDependencies,
  currentTheme: AppTheme,
  currentLanguage: AppLanguage,
  onResetApp: () -> Unit = {},
  onThemeChanged: (AppTheme) -> Unit = {},
  onLanguageChanged: (AppLanguage) -> Unit = {},
) {
  val features = rememberAppFeatures(dependencies)
  val memosState by features.memos.state.collectAsState()
  val habitsState by features.habits.state.collectAsState()
  val settingsState by features.settings.state.collectAsState()

  FeatureCoordinator(
    features = features,
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
    dependencies = dependencies,
    memosState = memosState,
    habitsState = habitsState,
    currentLanguage = currentLanguage,
    currentTheme = currentTheme,
  )

  SettingsDialog(state = settingsState, dispatch = features.settings::send)
  MemoCreateDialog(features.appState, features.memos::send)
  MemoEditDialog(features.appState, features.memos::send)
}
