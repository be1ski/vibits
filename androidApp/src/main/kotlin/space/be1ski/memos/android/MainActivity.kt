package space.be1ski.memos.android

import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import space.be1ski.memos.shared.presentation.app.MemosApp

/**
 * Android entry activity hosting shared Compose UI.
 */
class MainActivity : ComponentActivity() {
  /**
   * Sets up edge-to-edge and renders the shared Compose UI.
   */
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge(
      statusBarStyle = SystemBarStyle.light(
        Color.Transparent.toArgb(),
        Color.Transparent.toArgb()
      )
    )
    setContent {
      MemosApp()
    }
  }
}
