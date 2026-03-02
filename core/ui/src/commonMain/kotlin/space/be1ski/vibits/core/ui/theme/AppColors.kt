@file:Suppress("MagicNumber")

package space.be1ski.vibits.core.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * A pair of colors for light and dark themes.
 */
data class ColorPair(
  val light: Color,
  val dark: Color,
)

/**
 * Resolves the color based on the current system theme.
 * Uses LocalDarkTheme for efficient access without multiple subscriptions.
 */
@Composable
fun ColorPair.resolve(): Color = if (LocalDarkTheme.current) dark else light

/**
 * Application color palette with light/dark theme support.
 */
object AppColors {
  // Grid colors
  val inactiveCell =
    ColorPair(
      light = Color(0xFFE2E8F0),
      dark = Color(0xFF1E2530),
    )
  val todayHighlight =
    ColorPair(
      light = Color(0x18000000),
      dark = Color(0x20FFFFFF),
    )
  val habitGradientStart =
    ColorPair(
      light = Color(0xFFCFEED6),
      dark = Color(0xFF0F2318),
    )
  val habitGradientEnd =
    ColorPair(
      light = Color(0xFF0B7D3E),
      dark = Color(0xFF4ADE80),
    )

  // Status colors (success rate)
  val statusGreen =
    ColorPair(
      light = Color(0xFF4CAF50),
      dark = Color(0xFF4ADE80),
    )
  val statusYellow =
    ColorPair(
      light = Color(0xFFFFC107),
      dark = Color(0xFFFCD34D),
    )
  val statusRed =
    ColorPair(
      light = Color(0xFFE57373),
      dark = Color(0xFFF87171),
    )

  // Habit color palette
  val habitGreen =
    ColorPair(
      light = Color(0xFF4CAF50),
      dark = Color(0xFF4ADE80),
    )
  val habitBlue =
    ColorPair(
      light = Color(0xFF2196F3),
      dark = Color(0xFF60A5FA),
    )
  val habitPurple =
    ColorPair(
      light = Color(0xFF9C27B0),
      dark = Color(0xFFC084FC),
    )

  // Onboarding colors
  val background =
    ColorPair(
      light = Color(0xFFF0F2F5),
      dark = Color(0xFF121212),
    )
  val onBackground =
    ColorPair(
      light = Color(0xFF000000),
      dark = Color(0xFFFFFFFF),
    )
  val cardBackground =
    ColorPair(
      light = Color(0xFFFFFFFF),
      dark = Color(0xFF1E293B),
    )
  val onCardBackground =
    ColorPair(
      light = Color(0xFF1E293B),
      dark = Color(0xFFE2E8F0),
    )
  val cardSelected =
    ColorPair(
      light = Color(0xFFDEEBFF),
      dark = Color(0xFF1E3A5F),
    )
  val errorColor =
    ColorPair(
      light = Color(0xFFD32F2F),
      dark = Color(0xFFEF5350),
    )
}
