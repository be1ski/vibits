package space.be1ski.vibits.shared.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf

/**
 * CompositionLocal for the current dark theme state.
 * This allows efficient access to theme state without multiple subscriptions.
 */
val LocalDarkTheme = compositionLocalOf { false }

/**
 * Memos application theme with automatic dark/light mode support.
 * Uses platform-specific theme detection that updates dynamically.
 */
@Composable
fun VibitsTheme(
  darkTheme: Boolean = rememberSystemDarkTheme(),
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme()

  CompositionLocalProvider(LocalDarkTheme provides darkTheme) {
    MaterialTheme(
      colorScheme = colorScheme,
      content = content,
    )
  }
}
