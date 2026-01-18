package space.be1ski.vibits.web

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import space.be1ski.vibits.shared.app.di.AppGraph
import space.be1ski.vibits.shared.app.view.AppRoot

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
  val dependencies = AppGraph.createAppDependencies()
  ComposeViewport(document.getElementById("root")!!) { AppRoot(dependencies) }
}
