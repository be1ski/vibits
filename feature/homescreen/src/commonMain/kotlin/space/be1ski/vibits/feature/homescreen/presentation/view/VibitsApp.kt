package space.be1ski.vibits.feature.homescreen.presentation.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import space.be1ski.vibits.core.platform.isDesktop
import space.be1ski.vibits.core.platform.locale.AppLanguage
import space.be1ski.vibits.feature.homescreen.presentation.AppFeatures
import space.be1ski.vibits.feature.memos.domain.repository.ExportService
import space.be1ski.vibits.feature.settings.domain.model.AppTheme
import space.be1ski.vibits.feature.settings.presentation.view.SettingsDialog

@Composable
internal fun VibitsApp(
  features: AppFeatures,
  currentTheme: AppTheme,
  currentLanguage: AppLanguage,
  exportService: ExportService,
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

  if (isDesktop) {
    VibitsDesktopShell(
      features = features,
      appState = appState,
      memosState = memosState,
      habitsState = habitsState,
      currentLanguage = currentLanguage,
      currentTheme = currentTheme,
      syncDebounceSeconds = settingsState.selectedSyncDebounceSeconds,
    )
  } else {
    VibitsAppScaffold(
      features = features,
      appState = appState,
      memosState = memosState,
      habitsState = habitsState,
      currentLanguage = currentLanguage,
      currentTheme = currentTheme,
      syncDebounceSeconds = settingsState.selectedSyncDebounceSeconds,
    )
  }

  SettingsDialog(state = settingsState, dispatch = features.settings::send, exportService = exportService)
  MemoCreateDialog(state = memosState, dispatch = features.memos::send)
  MemoEditDialog(state = memosState, dispatch = features.memos::send)
  HabitsDialogs(appState = appState, habitsState = habitsState, dispatch = features.habits::send)
}
