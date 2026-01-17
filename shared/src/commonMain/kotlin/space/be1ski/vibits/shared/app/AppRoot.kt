package space.be1ski.vibits.shared.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import org.koin.compose.koinInject
import space.be1ski.vibits.shared.core.ui.theme.VibitsTheme
import space.be1ski.vibits.shared.core.ui.theme.rememberSystemDarkTheme
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode
import space.be1ski.vibits.shared.feature.mode.presentation.ModeSelectionScreen
import space.be1ski.vibits.shared.feature.settings.domain.model.AppTheme

@Composable
fun AppRoot() {
  val stateHolder: AppRootStateHolder = koinInject()

  val appMode by stateHolder.appMode.collectAsState()
  val appTheme by stateHolder.appTheme.collectAsState()
  val appLanguage by stateHolder.appLanguage.collectAsState()

  val systemDarkTheme = rememberSystemDarkTheme()
  val darkTheme =
    when (appTheme) {
      AppTheme.SYSTEM -> systemDarkTheme
      AppTheme.LIGHT -> false
      AppTheme.DARK -> true
    }

  // Use key to force full recomposition when language changes
  key(appLanguage) {
    VibitsTheme(darkTheme = darkTheme) {
      when (appMode) {
        AppMode.NOT_SELECTED -> {
          ModeSelectionScreen(
            onModeSelected = { selectedMode ->
              stateHolder.saveAppMode(selectedMode)
            },
          )
        }
        AppMode.ONLINE, AppMode.OFFLINE, AppMode.DEMO -> {
          VibitsApp(
            currentTheme = appTheme,
            currentLanguage = appLanguage,
            onResetApp = { stateHolder.resetApp() },
            onThemeChanged = { theme -> stateHolder.updateTheme(theme) },
            onLanguageChanged = { language -> stateHolder.updateLanguage(language) },
          )
        }
      }
    }
  }
}
