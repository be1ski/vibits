package space.be1ski.vibits.shared.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.koin.compose.koinInject
import space.be1ski.vibits.shared.core.platform.LocaleProvider
import space.be1ski.vibits.shared.core.ui.theme.VibitsTheme
import space.be1ski.vibits.shared.core.ui.theme.rememberSystemDarkTheme
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode
import space.be1ski.vibits.shared.feature.mode.domain.usecase.FixInvalidOnlineModeUseCase
import space.be1ski.vibits.shared.feature.mode.domain.usecase.SaveAppModeUseCase
import space.be1ski.vibits.shared.feature.mode.presentation.ModeSelectionScreen
import space.be1ski.vibits.shared.feature.preferences.domain.model.AppTheme
import space.be1ski.vibits.shared.feature.preferences.domain.usecase.LoadPreferencesUseCase

// Suppress false positive: mutableStateOf assignments trigger recomposition
@Suppress("AssignedValueIsNeverRead")
@Composable
fun AppRoot() {
  val saveAppModeUseCase: SaveAppModeUseCase = koinInject()
  val fixInvalidOnlineModeUseCase: FixInvalidOnlineModeUseCase = koinInject()
  val loadPreferencesUseCase: LoadPreferencesUseCase = koinInject()
  val localeProvider: LocaleProvider = koinInject()

  // Configure locale and load initial theme before first composition
  val initialPrefs = remember { loadPreferencesUseCase() }
  remember { localeProvider.configureLocale(initialPrefs.language) }

  var appMode by remember { mutableStateOf(fixInvalidOnlineModeUseCase()) }
  var appTheme by remember { mutableStateOf(initialPrefs.theme) }

  val systemDarkTheme = rememberSystemDarkTheme()
  val darkTheme =
    when (appTheme) {
      AppTheme.SYSTEM -> systemDarkTheme
      AppTheme.LIGHT -> false
      AppTheme.DARK -> true
    }

  VibitsTheme(darkTheme = darkTheme) {
    when (appMode) {
      AppMode.NOT_SELECTED -> {
        ModeSelectionScreen(
          onModeSelected = { selectedMode ->
            saveAppModeUseCase(selectedMode)
            appMode = selectedMode
          },
        )
      }
      AppMode.ONLINE, AppMode.OFFLINE, AppMode.DEMO -> {
        VibitsApp(
          onResetApp = { appMode = AppMode.NOT_SELECTED },
          onThemeChanged = { theme -> appTheme = theme },
        )
      }
    }
  }
}
