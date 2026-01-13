package space.be1ski.memos.desktop

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import org.koin.core.context.startKoin
import space.be1ski.memos.shared.di.sharedModule
import space.be1ski.memos.shared.presentation.app.MemosApp

/**
 * Desktop entry point that starts Koin and opens the main window.
 */
fun main() = application {
  startKoin {
    modules(sharedModule())
  }

  Window(
    onCloseRequest = ::exitApplication,
    title = "Memos",
    state = rememberWindowState(width = 720.dp, height = 960.dp)
  ) {
    MemosApp()
  }
}
