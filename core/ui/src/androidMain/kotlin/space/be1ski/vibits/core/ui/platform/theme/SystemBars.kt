package space.be1ski.vibits.core.ui.platform.theme

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun ConfigureSystemBars(darkTheme: Boolean) {
  val context = LocalContext.current
  SideEffect {
    val activity = context as? ComponentActivity ?: return@SideEffect
    configureEdgeToEdge(activity, darkTheme)
  }
}

private fun configureEdgeToEdge(
  activity: Activity,
  darkTheme: Boolean,
) {
  if (activity !is ComponentActivity) return
  val transparent = Color.Transparent.toArgb()
  val statusBarStyle =
    if (darkTheme) {
      SystemBarStyle.dark(transparent)
    } else {
      SystemBarStyle.light(transparent, transparent)
    }
  activity.enableEdgeToEdge(statusBarStyle = statusBarStyle)
}
