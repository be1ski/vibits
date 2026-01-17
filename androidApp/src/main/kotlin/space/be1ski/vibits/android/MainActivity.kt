package space.be1ski.vibits.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import org.koin.core.context.GlobalContext
import space.be1ski.vibits.shared.app.AppRoot
import space.be1ski.vibits.shared.di.createAppDependencies

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge(
      statusBarStyle =
        SystemBarStyle.auto(
          Color.Transparent.toArgb(),
          Color.Transparent.toArgb(),
        ),
    )
    val dependencies = GlobalContext.get().createAppDependencies()
    setContent {
      AppRoot(dependencies)
    }
  }
}
