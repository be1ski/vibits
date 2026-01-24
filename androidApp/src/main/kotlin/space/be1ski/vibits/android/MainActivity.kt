package space.be1ski.vibits.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import space.be1ski.vibits.shared.app.view.AppRoot
import space.be1ski.vibits.shared.app.di.AppGraph

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    val dependencies = AppGraph.createAppDependencies()
    setContent {
      AppRoot(dependencies)
    }
  }
}
