package space.be1ski.vibits.desktop

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import space.be1ski.vibits.desktop.view.VibitsMenuBar
import space.be1ski.vibits.feature.homescreen.di.AppGraph
import space.be1ski.vibits.feature.homescreen.domain.model.Screen
import space.be1ski.vibits.feature.homescreen.presentation.AppFeatures
import space.be1ski.vibits.feature.homescreen.presentation.view.AppRoot
import java.awt.Dimension
import java.awt.Taskbar
import javax.imageio.ImageIO

private const val MIN_WINDOW_WIDTH = 640
private const val MIN_WINDOW_HEIGHT = 480
private val POSTS_LIST_HEIGHT = 200.dp

fun main() {
  setDockIcon()
  return application {
    val dependencies = AppGraph.createAppDependencies()
    var features by remember { mutableStateOf<AppFeatures?>(null) }
    val windowState =
      rememberWindowState(
        width = 900.dp,
        height = 700.dp,
      )

    features?.let { f ->
      val appState by f.app.state.collectAsState()
      val postsVisible = appState.selectedScreen == Screen.STATS && appState.postsListExpanded
      var previousPostsVisible by remember { mutableStateOf(postsVisible) }
      LaunchedEffect(postsVisible) {
        if (postsVisible != previousPostsVisible) {
          val delta = if (postsVisible) POSTS_LIST_HEIGHT else -POSTS_LIST_HEIGHT
          windowState.size =
            DpSize(
              width = windowState.size.width,
              height = (windowState.size.height + delta).coerceAtLeast(MIN_WINDOW_HEIGHT.dp),
            )
          previousPostsVisible = postsVisible
        }
      }
    }

    Window(
      onCloseRequest = ::exitApplication,
      title = "Vibits",
      state = windowState,
      icon = painterResource("icon.png"),
    ) {
      window.minimumSize = Dimension(MIN_WINDOW_WIDTH, MIN_WINDOW_HEIGHT)
      features?.let { VibitsMenuBar(it) }
      AppRoot(
        dependencies = dependencies,
        onFeaturesReady = { features = it },
      )
    }
  }
}

private fun setDockIcon() {
  if (!Taskbar.isTaskbarSupported()) return
  val taskbar = Taskbar.getTaskbar()
  if (taskbar.isSupported(Taskbar.Feature.ICON_IMAGE)) {
    object {}
      .javaClass
      .getResourceAsStream("/icon_dock.png")
      ?.use { taskbar.iconImage = ImageIO.read(it) }
  }
}
