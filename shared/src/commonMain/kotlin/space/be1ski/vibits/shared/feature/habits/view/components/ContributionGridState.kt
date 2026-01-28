package space.be1ski.vibits.shared.feature.habits.view.components

import androidx.compose.ui.unit.Dp

/**
 * Calculates layout sizes for a fixed number of columns.
 */
internal fun calculateLayout(
  maxWidth: Dp,
  columns: Int,
  minColumnSize: Dp,
  spacing: Dp,
  maxColumnSize: Dp? = null,
): ChartLayout {
  val safeColumns = columns.coerceAtLeast(1)
  val totalSpacing = spacing * (safeColumns - 1)
  val calculated = (maxWidth - totalSpacing) / safeColumns
  val useScroll = calculated < minColumnSize
  val capped = maxColumnSize?.let { calculated.coerceAtMost(it) } ?: calculated
  val columnSize = if (useScroll) minColumnSize else capped
  val contentWidth = columnSize * safeColumns + totalSpacing
  return ChartLayout(columnSize = columnSize, contentWidth = contentWidth, useScroll = useScroll)
}
