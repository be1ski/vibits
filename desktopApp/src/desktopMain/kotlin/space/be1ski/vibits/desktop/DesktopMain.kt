package space.be1ski.vibits.desktop

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import space.be1ski.vibits.shared.app.di.AppGraph
import space.be1ski.vibits.shared.app.view.AppRoot

fun main() =
  application {
    val dependencies = AppGraph.createAppDependencies()

    Window(
      onCloseRequest = ::exitApplication,
      title = "Vibits",
      state = rememberWindowState(width = 540.dp, height = 1080.dp),
    ) {
      AppRoot(dependencies)
    }
  }
