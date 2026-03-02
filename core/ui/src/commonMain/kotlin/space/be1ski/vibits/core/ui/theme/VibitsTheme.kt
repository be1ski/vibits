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
val LocalWideLayout = compositionLocalOf { false }

@Composable
fun VibitsTheme(
  darkTheme: Boolean = rememberSystemDarkTheme(),
  wideLayout: Boolean = isDesktop,
  content: @Composable () -> Unit,
) {
  ConfigureSystemBars(darkTheme)
  val colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme()

  CompositionLocalProvider(
    LocalDarkTheme provides darkTheme,
    LocalWideLayout provides wideLayout,
  ) {
    MaterialTheme(
      colorScheme = colorScheme,
      content = content,
    )
  }
}
