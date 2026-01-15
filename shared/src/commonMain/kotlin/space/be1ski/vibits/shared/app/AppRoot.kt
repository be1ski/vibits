package space.be1ski.vibits.shared.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.koin.compose.koinInject
import space.be1ski.vibits.shared.core.ui.theme.VibitsTheme
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode
import space.be1ski.vibits.shared.feature.mode.domain.usecase.LoadAppModeUseCase
import space.be1ski.vibits.shared.feature.mode.domain.usecase.SaveAppModeUseCase
import space.be1ski.vibits.shared.feature.mode.presentation.ModeSelectionScreen

@Composable
fun AppRoot() {
  val loadAppModeUseCase: LoadAppModeUseCase = koinInject()
  val saveAppModeUseCase: SaveAppModeUseCase = koinInject()

  var appMode by remember { mutableStateOf(loadAppModeUseCase()) }

  VibitsTheme {
    when (appMode) {
      AppMode.NotSelected -> {
        ModeSelectionScreen(
          onModeSelected = { selectedMode ->
            saveAppModeUseCase(selectedMode)
            appMode = selectedMode
          }
        )
      }
      AppMode.Online, AppMode.Offline -> {
        VibitsApp(
          onResetApp = { appMode = AppMode.NotSelected }
        )
      }
    }
  }
}
