package space.be1ski.memos.shared.presentation

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController
import space.be1ski.memos.shared.di.initKoin
import space.be1ski.memos.shared.presentation.app.MemosApp

/**
 * Entry point for embedding Compose UI into an iOS host.
 */
@Suppress("FunctionNaming")
fun MainViewController(): UIViewController {
  initKoin()
  return ComposeUIViewController { MemosApp() }
}
