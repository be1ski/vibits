package space.be1ski.vibits.desktop

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import org.koin.core.context.startKoin
import space.be1ski.vibits.shared.app.AppRoot
import space.be1ski.vibits.shared.di.sharedModule

fun main() =
  application {
    startKoin {
      modules(sharedModule())
    }

    Window(
      onCloseRequest = ::exitApplication,
      title = "Vibits",
      state = rememberWindowState(width = 540.dp, height = 1080.dp),
    ) {
      AppRoot()
    }
  }
