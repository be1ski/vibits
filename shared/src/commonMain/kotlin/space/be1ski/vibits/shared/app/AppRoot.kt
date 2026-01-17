package space.be1ski.vibits.shared.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.koin.compose.koinInject
import space.be1ski.vibits.shared.core.ui.theme.VibitsTheme
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode
import space.be1ski.vibits.shared.feature.mode.domain.usecase.FixInvalidOnlineModeUseCase
import space.be1ski.vibits.shared.feature.mode.domain.usecase.SaveAppModeUseCase
import space.be1ski.vibits.shared.feature.mode.presentation.ModeSelectionScreen

// Suppress false positive: mutableStateOf assignments trigger recomposition
@Suppress("AssignedValueIsNeverRead")
@Composable
fun AppRoot() {
  val saveAppModeUseCase: SaveAppModeUseCase = koinInject()
  val fixInvalidOnlineModeUseCase: FixInvalidOnlineModeUseCase = koinInject()

  var appMode by remember { mutableStateOf(fixInvalidOnlineModeUseCase()) }

  VibitsTheme {
    when (appMode) {
      AppMode.NOT_SELECTED -> {
        ModeSelectionScreen(
          onModeSelected = { selectedMode ->
            saveAppModeUseCase(selectedMode)
            appMode = selectedMode
          }
        )
      }
      AppMode.ONLINE, AppMode.OFFLINE, AppMode.DEMO -> {
        VibitsApp(
          onResetApp = { appMode = AppMode.NOT_SELECTED }
        )
      }
    }
  }
}
