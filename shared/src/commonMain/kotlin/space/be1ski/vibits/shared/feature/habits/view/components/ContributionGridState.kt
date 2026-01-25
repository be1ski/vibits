package space.be1ski.vibits.shared.feature.habits.view.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import space.be1ski.vibits.shared.app.di.AppScope
import space.be1ski.vibits.shared.app.domain.model.ActivityMode
import space.be1ski.vibits.shared.app.domain.model.ActivityRange
import space.be1ski.vibits.shared.feature.habits.domain.model.ActivityWeekData
import space.be1ski.vibits.shared.feature.habits.domain.model.DailyMemoInfo
import space.be1ski.vibits.shared.feature.habits.domain.model.HabitsConfigEntry
import space.be1ski.vibits.shared.feature.habits.domain.usecase.BuildActivityDataUseCase
import space.be1ski.vibits.shared.feature.habits.domain.usecase.ExtractDailyMemosUseCase
import space.be1ski.vibits.shared.feature.habits.domain.usecase.ExtractHabitsConfigUseCase
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo

private val emptyWeekData = ActivityWeekData(weeks = emptyList(), maxDaily = 0, maxWeekly = 0)

/**
 * Result of [rememberActivityWeekData] with loading state.
 */
data class ActivityWeekDataState(
  val data: ActivityWeekData,
  val isLoading: Boolean,
)

/**
 * Runtime cache for ActivityWeekData.
 * When memos list changes, saves snapshot of old cache before clearing.
 * Snapshot is used to prevent UI flashing while recalculating new data.
 * Exposes [version] to trigger LaunchedEffect restart when memos reference changes.
 */
@Inject
@SingleIn(AppScope::class)
class ActivityWeekDataCache {
  private var lastMemos: List<Memo>? = null
  private val cache = mutableMapOf<Pair<ActivityRange, ActivityMode>, ActivityWeekData>()
  private var snapshot = mutableMapOf<Pair<ActivityRange, ActivityMode>, ActivityWeekData>()

  /**
   * Increments when memos reference changes.
   * Use as LaunchedEffect key to ensure recomputation.
   */
  var version: Int = 0
    private set

  fun get(
    memos: List<Memo>,
    range: ActivityRange,
    mode: ActivityMode,
  ): ActivityWeekData? {
    val sameRef = memos === lastMemos
    if (!sameRef) {
      // Save snapshot before clearing to prevent UI flashing
      snapshot = cache.toMutableMap()
      cache.clear()
      lastMemos = memos
      version++
    }
    // Return from cache if available, otherwise from snapshot
    return cache[range to mode] ?: snapshot[range to mode]
  }

  fun isFresh(
    memos: List<Memo>,
    range: ActivityRange,
    mode: ActivityMode,
  ): Boolean {
    return memos === lastMemos && cache.containsKey(range to mode)
  }

  fun put(
    memos: List<Memo>,
    range: ActivityRange,
    mode: ActivityMode,
    data: ActivityWeekData,
  ) {
    if (memos !== lastMemos) {
      snapshot = cache.toMutableMap()
      cache.clear()
      lastMemos = memos
      version++
    }
    cache[range to mode] = data
    // Clear snapshot for this key once new data is ready
    snapshot.remove(range to mode)
  }

  fun clear() {
    cache.clear()
    snapshot.clear()
    lastMemos = null
    version++
  }
}

/**
 * Memoized builder for [ActivityWeekData].
 * Pre-extracts config and daily memos (cached by memos only), then builds range-dependent data.
 * Computation runs in background thread; caches results per range for instant switching.
 * Never shows loading state for background recalculation to prevent UI flashing.
 */
@Composable
fun rememberActivityWeekData(
  memos: List<Memo>,
  range: ActivityRange,
  mode: ActivityMode,
  today: LocalDate,
  buildActivityDataUseCase: BuildActivityDataUseCase,
  cache: ActivityWeekDataCache,
): ActivityWeekDataState {
  val timeZone = remember { TimeZone.currentSystemDefault() }
  // These are cached by memos only - won't recompute on range change
  val configTimeline = rememberHabitsConfigTimeline(memos)
  val dailyMemos = rememberDailyMemos(memos)

  // CRITICAL: Capture cacheVersion first, then get data
  // This ensures remember() key matches the data we're initializing with
  val cacheVersion = cache.version
  val cachedData = cache.get(memos, range, mode)

  // Always use remember to hold current data, initializing with cached/snapshot data
  var currentData by remember(cacheVersion, range, mode) {
    mutableStateOf(cachedData ?: emptyWeekData)
  }

  // Trigger background recalculation if data is not fresh
  LaunchedEffect(cacheVersion, range, mode) {
    // Skip if we already have fresh data in main cache
    if (cache.isFresh(memos, range, mode)) {
      return@LaunchedEffect
    }

    val result =
      withContext(Dispatchers.Default) {
        buildActivityDataUseCase.buildWeekData(
          configTimeline = configTimeline,
          dailyMemos = dailyMemos,
          timeZone = timeZone,
          memos = memos,
          range = range,
          mode = mode,
          today = today,
        )
      }
    cache.put(memos, range, mode, result)
    currentData = result
  }

  return ActivityWeekDataState(data = currentData, isLoading = false)
}

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
