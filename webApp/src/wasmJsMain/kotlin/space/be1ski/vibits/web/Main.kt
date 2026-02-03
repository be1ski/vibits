package space.be1ski.vibits.web

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import space.be1ski.vibits.feature.main.di.AppGraph
import space.be1ski.vibits.feature.main.view.AppRoot

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
  val dependencies = AppGraph.createAppDependencies()
  ComposeViewport(document.getElementById("root")!!) { AppRoot(dependencies) }
}
