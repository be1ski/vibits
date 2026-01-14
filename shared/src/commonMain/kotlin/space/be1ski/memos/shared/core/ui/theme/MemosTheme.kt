package space.be1ski.memos.shared.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * Memos application theme with automatic dark/light mode support.
 * Uses platform-specific theme detection that updates dynamically.
 */
@Composable
fun MemosTheme(
  darkTheme: Boolean = rememberSystemDarkTheme(),
  content: @Composable () -> Unit
) {
  val colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme()

  MaterialTheme(
    colorScheme = colorScheme,
    content = content
  )
}
