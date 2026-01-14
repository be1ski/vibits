package space.be1ski.memos.shared.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.koin.compose.koinInject
import space.be1ski.memos.shared.feature.mode.domain.model.AppMode
import space.be1ski.memos.shared.feature.mode.domain.usecase.LoadAppModeUseCase
import space.be1ski.memos.shared.feature.mode.domain.usecase.SaveAppModeUseCase
import space.be1ski.memos.shared.feature.mode.presentation.ModeSelectionScreen

@Composable
fun AppRoot() {
  val loadAppModeUseCase: LoadAppModeUseCase = koinInject()
  val saveAppModeUseCase: SaveAppModeUseCase = koinInject()

  var appMode by remember { mutableStateOf(loadAppModeUseCase()) }

  MaterialTheme {
    when (appMode) {
      AppMode.NotSelected -> {
        ModeSelectionScreen(
          onModeSelected = { selectedMode ->
            saveAppModeUseCase(selectedMode)
            appMode = selectedMode
          }
        )
      }
      AppMode.Online -> {
        MemosApp()
      }
      AppMode.Offline -> {
        MemosApp()
      }
    }
  }
}
