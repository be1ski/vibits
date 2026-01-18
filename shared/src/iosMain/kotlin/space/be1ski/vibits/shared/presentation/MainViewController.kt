package space.be1ski.vibits.shared.presentation

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController
import space.be1ski.vibits.shared.app.di.AppGraph
import space.be1ski.vibits.shared.app.view.AppRoot

/**
 * Entry point for embedding Compose UI into an iOS host.
 */
@Suppress("FunctionNaming", "ktlint:standard:function-naming")
fun MainViewController(): UIViewController {
  val dependencies = AppGraph.createAppDependencies()
  return ComposeUIViewController { AppRoot(dependencies) }
}
