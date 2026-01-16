package space.be1ski.vibits.shared.feature.habits.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo
import space.be1ski.vibits.shared.core.ui.ActivityMode
import space.be1ski.vibits.shared.core.ui.ActivityRange
import space.be1ski.vibits.shared.feature.habits.domain.model.ActivityWeek
import space.be1ski.vibits.shared.feature.habits.domain.model.ActivityWeekData
import space.be1ski.vibits.shared.feature.habits.domain.model.DailyMemoInfo
import space.be1ski.vibits.shared.feature.habits.domain.model.HabitStatus
import space.be1ski.vibits.shared.feature.habits.domain.model.HabitsConfigEntry
import space.be1ski.vibits.shared.feature.habits.domain.model.RangeBounds
import space.be1ski.vibits.shared.feature.habits.domain.model.rangeBounds

private const val DAYS_IN_WEEK = 7

internal data class DayDataContext(
  val date: LocalDate,
  val bounds: RangeBounds,
  val mode: ActivityMode,
  val configTimeline: List<HabitsConfigEntry>,
  val dailyMemos: Map<LocalDate, DailyMemoInfo>,
  val counts: Map<LocalDate, Int>
)

internal data class HabitSelectionState(
  val useHabits: Boolean,
  val habitStatuses: List<HabitStatus>,
  val memoHabitTags: Set<String>,
  val configTags: Set<String>
)

private val emptyWeekData = ActivityWeekData(weeks = emptyList(), maxDaily = 0, maxWeekly = 0)

/**
 * Result of [rememberActivityWeekData] with loading state.
 */
data class ActivityWeekDataState(
  val data: ActivityWeekData,
  val isLoading: Boolean
)

/**
 * Runtime cache for ActivityWeekData.
 * Clears automatically when memos list changes (by reference).
 * Exposes [version] to trigger LaunchedEffect restart when memos reference changes.
 */
class ActivityWeekDataCache {
  private var lastMemos: List<Memo>? = null
  private val cache = mutableMapOf<Pair<ActivityRange, ActivityMode>, ActivityWeekData>()

  /**
   * Increments when memos reference changes.
   * Use as LaunchedEffect key to ensure recomputation.
   */
  var version: Int = 0
    private set

  fun get(memos: List<Memo>, range: ActivityRange, mode: ActivityMode): ActivityWeekData? {
    val sameRef = memos === lastMemos
    if (!sameRef) {
      cache.clear()
      lastMemos = memos
      version++
      return null
    }
    return cache[range to mode]
  }

  fun put(memos: List<Memo>, range: ActivityRange, mode: ActivityMode, data: ActivityWeekData) {
    if (memos !== lastMemos) {
      cache.clear()
      lastMemos = memos
      version++
    }
    cache[range to mode] = data
  }

  fun clear() {
    cache.clear()
    lastMemos = null
    version++
  }
}

/**
 * CompositionLocal for [ActivityWeekDataCache].
 * Provided at app level, survives tab switches and recomposition.
 */
val LocalActivityWeekDataCache = compositionLocalOf { ActivityWeekDataCache() }

/**
 * Memoized builder for [ActivityWeekData].
 * Pre-extracts config and daily memos (cached by memos only), then builds range-dependent data.
 * Computation runs in background thread; caches results per range for instant switching.
 * Uses cache from [LocalActivityWeekDataCache] that survives tab switches.
 */
@Composable
fun rememberActivityWeekData(
  memos: List<Memo>,
  range: ActivityRange,
  mode: ActivityMode
): ActivityWeekDataState {
  val cache = LocalActivityWeekDataCache.current
  val timeZone = remember { TimeZone.currentSystemDefault() }
  // These are cached by memos only - won't recompute on range change
  val configTimeline = rememberHabitsConfigTimeline(memos)
  val dailyMemos = rememberDailyMemos(memos)

  // Check cache SYNCHRONOUSLY on every composition
  // This ensures we pick up cached data even if local state got reset during recomposition
  val cachedData = cache.get(memos, range, mode)

  // If cache has data, return immediately - no shimmer needed
  if (cachedData != null) {
    return ActivityWeekDataState(data = cachedData, isLoading = false)
  }

  // Cache miss - need to load in background
  val cacheVersion = cache.version
  var currentData by remember(cacheVersion, range, mode) { mutableStateOf(emptyWeekData) }
  var isLoading by remember(cacheVersion, range, mode) { mutableStateOf(true) }

  LaunchedEffect(cacheVersion, range, mode) {
    val result = withContext(Dispatchers.Default) {
      buildActivityWeekData(configTimeline, dailyMemos, timeZone, memos, range, mode)
    }
    cache.put(memos, range, mode, result)
    currentData = result
    isLoading = false
  }

  return ActivityWeekDataState(data = currentData, isLoading = isLoading)
}

/**
 * Memoized builder for habits config timeline.
 */
@Composable
fun rememberHabitsConfigTimeline(memos: List<Memo>): List<HabitsConfigEntry> {
  val timeZone = remember { TimeZone.currentSystemDefault() }
  return remember(memos, timeZone) {
    extractHabitsConfigEntries(memos, timeZone)
  }
}

/**
 * Memoized builder for daily memos map.
 */
@Composable
fun rememberDailyMemos(memos: List<Memo>): Map<LocalDate, DailyMemoInfo> {
  val timeZone = remember { TimeZone.currentSystemDefault() }
  return remember(memos, timeZone) {
    extractDailyMemos(memos, timeZone)
  }
}

/**
 * Calculates layout sizes for a fixed number of columns.
 */
internal fun calculateLayout(
  maxWidth: Dp,
  columns: Int,
  minColumnSize: Dp,
  spacing: Dp
): ChartLayout {
  val safeColumns = columns.coerceAtLeast(1)
  val totalSpacing = spacing * (safeColumns - 1)
  val calculated = (maxWidth - totalSpacing) / safeColumns
  val useScroll = calculated < minColumnSize
  val columnSize = if (useScroll) minColumnSize else calculated
  val contentWidth = if (useScroll) columnSize * safeColumns + totalSpacing else maxWidth
  return ChartLayout(columnSize = columnSize, contentWidth = contentWidth, useScroll = useScroll)
}

/**
 * Builds the chart dataset for a given [range].
 * Uses pre-extracted configTimeline and dailyMemos to avoid redundant work on range change.
 */
@Suppress("LongParameterList")
private fun buildActivityWeekData(
  configTimeline: List<HabitsConfigEntry>,
  dailyMemos: Map<LocalDate, DailyMemoInfo>,
  timeZone: TimeZone,
  memos: List<Memo>,
  range: ActivityRange,
  mode: ActivityMode
): ActivityWeekData {
  val bounds = rangeBounds(range)
  val effectiveConfigTimeline = if (mode == ActivityMode.Habits) configTimeline else emptyList()
  val counts = if (mode == ActivityMode.Posts) extractDailyPostCounts(memos, timeZone, bounds) else emptyMap()

  val start = startOfWeek(bounds.start)
  val weeks = mutableListOf<ActivityWeek>()
  var cursor = start
  while (cursor <= bounds.end) {
    val days = (0 until DAYS_IN_WEEK).map { offset ->
      buildDayData(
        DayDataContext(
          date = cursor.plus(DatePeriod(days = offset)),
          bounds = bounds,
          mode = mode,
          configTimeline = effectiveConfigTimeline,
          dailyMemos = dailyMemos,
          counts = counts
        )
      )
    }
    weeks.add(
      ActivityWeek(
        startDate = cursor,
        days = days,
        weeklyCount = days.sumOf { it.count }
      )
    )
    cursor = cursor.plus(DatePeriod(days = DAYS_IN_WEEK))
  }
  val maxDaily = weeks.maxOfOrNull { week -> week.days.maxOfOrNull { it.count } ?: 0 } ?: 0
  val maxWeekly = weeks.maxOfOrNull { it.weeklyCount } ?: 0
  return ActivityWeekData(weeks = weeks, maxDaily = maxDaily, maxWeekly = maxWeekly)
}
