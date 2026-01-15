@file:Suppress("MagicNumber")

package space.be1ski.memos.shared.core.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * A pair of colors for light and dark themes.
 */
data class ColorPair(val light: Color, val dark: Color)

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
  val inactiveCell = ColorPair(
    light = Color(0xFFE2E8F0),
    dark = Color(0xFF1E2530)
  )
  val todayHighlight = ColorPair(
    light = Color(0x18000000),
    dark = Color(0x20FFFFFF)
  )
  val habitGradientStart = ColorPair(
    light = Color(0xFFCFEED6),
    dark = Color(0xFF0F2318)
  )
  val habitGradientEnd = ColorPair(
    light = Color(0xFF0B7D3E),
    dark = Color(0xFF4ADE80)
  )

  // Status colors (success rate)
  val statusGreen = ColorPair(
    light = Color(0xFF4CAF50),
    dark = Color(0xFF4ADE80)
  )
  val statusYellow = ColorPair(
    light = Color(0xFFFFC107),
    dark = Color(0xFFFCD34D)
  )
  val statusRed = ColorPair(
    light = Color(0xFFE57373),
    dark = Color(0xFFF87171)
  )

  // Habit color palette
  val habitGreen = ColorPair(
    light = Color(0xFF4CAF50),
    dark = Color(0xFF4ADE80)
  )
  val habitBlue = ColorPair(
    light = Color(0xFF2196F3),
    dark = Color(0xFF60A5FA)
  )
  val habitRed = ColorPair(
    light = Color(0xFFF44336),
    dark = Color(0xFFF87171)
  )
  val habitOrange = ColorPair(
    light = Color(0xFFFF9800),
    dark = Color(0xFFFBBF24)
  )
  val habitPurple = ColorPair(
    light = Color(0xFF9C27B0),
    dark = Color(0xFFC084FC)
  )
  val habitCyan = ColorPair(
    light = Color(0xFF00BCD4),
    dark = Color(0xFF22D3EE)
  )
  val habitPink = ColorPair(
    light = Color(0xFFE91E63),
    dark = Color(0xFFF472B6)
  )
  val habitBrown = ColorPair(
    light = Color(0xFF795548),
    dark = Color(0xFFA8A29E)
  )
  val habitBlueGrey = ColorPair(
    light = Color(0xFF607D8B),
    dark = Color(0xFF94A3B8)
  )
  val habitYellow = ColorPair(
    light = Color(0xFFFFEB3B),
    dark = Color(0xFFFDE047)
  )

}
