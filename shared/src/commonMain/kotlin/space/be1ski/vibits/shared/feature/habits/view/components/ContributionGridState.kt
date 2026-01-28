package space.be1ski.vibits.shared.feature.habits.view.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import space.be1ski.vibits.shared.feature.habits.domain.model.DailyMemoInfo
import space.be1ski.vibits.shared.feature.habits.domain.model.HabitsConfigEntry
import space.be1ski.vibits.shared.feature.habits.domain.usecase.ExtractDailyMemosUseCase
import space.be1ski.vibits.shared.feature.habits.domain.usecase.ExtractHabitsConfigUseCase
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo

/**
 * Memoized builder for habits config timeline.
 */
@Composable
fun rememberHabitsConfigTimeline(memos: List<Memo>): List<HabitsConfigEntry> {
  val timeZone = remember { TimeZone.currentSystemDefault() }
  return remember(memos, timeZone) {
    ExtractHabitsConfigUseCase(memos, timeZone)
  }
}

/**
 * Memoized builder for daily memos map.
 */
@Composable
fun rememberDailyMemos(memos: List<Memo>): Map<LocalDate, DailyMemoInfo> {
  val timeZone = remember { TimeZone.currentSystemDefault() }
  return remember(memos, timeZone) {
    ExtractDailyMemosUseCase(memos, timeZone)
  }
}

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
