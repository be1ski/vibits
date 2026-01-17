package space.be1ski.vibits.web

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import org.koin.core.context.startKoin
import space.be1ski.vibits.shared.app.AppRoot
import space.be1ski.vibits.shared.di.sharedModule

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
  runCatching {
    startKoin { modules(sharedModule()) }
  }
  ComposeViewport(document.getElementById("root")!!) { AppRoot() }
}
