package space.be1ski.vibits.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import space.be1ski.vibits.core.platform.isDesktop
import space.be1ski.vibits.core.ui.platform.theme.ConfigureSystemBars
import space.be1ski.vibits.core.ui.platform.theme.rememberSystemDarkTheme

val LocalDarkTheme = compositionLocalOf { false }
val LocalIsDesktop = compositionLocalOf { false }

@Composable
fun VibitsTheme(
  darkTheme: Boolean = rememberSystemDarkTheme(),
  isDesktop: Boolean = space.be1ski.vibits.core.platform.isDesktop,
  content: @Composable () -> Unit,
) {
  ConfigureSystemBars(darkTheme)
  val colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme()

  CompositionLocalProvider(
    LocalDarkTheme provides darkTheme,
    LocalIsDesktop provides isDesktop,
  ) {
    MaterialTheme(
      colorScheme = colorScheme,
      content = content,
    )
  }
}
