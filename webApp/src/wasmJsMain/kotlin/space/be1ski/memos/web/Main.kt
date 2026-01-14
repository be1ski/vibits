package space.be1ski.memos.web

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import org.koin.core.context.startKoin
import space.be1ski.memos.shared.di.sharedModule
import space.be1ski.memos.shared.app.MemosApp

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
  runCatching {
    startKoin { modules(sharedModule()) }
  }
  ComposeViewport(document.getElementById("root")!!) { MemosApp() }
}
