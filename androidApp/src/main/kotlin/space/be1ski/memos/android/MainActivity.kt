package space.be1ski.memos.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import space.be1ski.memos.shared.presentation.MemosApp

/**
 * Android entry activity hosting shared Compose UI.
 */
class MainActivity : ComponentActivity() {
  /**
   * Sets up edge-to-edge and renders [space.be1ski.memos.shared.presentation.MemosApp].
   */
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MemosApp()
    }
  }
}
