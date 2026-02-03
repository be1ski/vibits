package space.be1ski.vibits.feature.main.presentation

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController
import space.be1ski.vibits.feature.main.di.AppGraph
import space.be1ski.vibits.feature.main.view.AppRoot

/**
 * Entry point for embedding Compose UI into an iOS host.
 */
@Suppress("FunctionNaming", "ktlint:standard:function-naming")
fun MainViewController(): UIViewController {
  val dependencies = AppGraph.createAppDependencies()
  return ComposeUIViewController { AppRoot(dependencies) }
}
