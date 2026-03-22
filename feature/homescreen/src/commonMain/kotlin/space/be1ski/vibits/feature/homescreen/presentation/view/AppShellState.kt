package space.be1ski.vibits.feature.homescreen.presentation.view

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import space.be1ski.vibits.core.platform.date.currentLocalDate
import space.be1ski.vibits.core.platform.mode.AppMode
import space.be1ski.vibits.core.ui.date.DateFormatter
import space.be1ski.vibits.core.ui.date.rememberDateFormatter
import space.be1ski.vibits.feature.habits.domain.model.ActivityMode
import space.be1ski.vibits.feature.habits.domain.model.ActivityRange
import space.be1ski.vibits.feature.habits.domain.model.HabitsConfigEntry
import space.be1ski.vibits.feature.habits.presentation.action.HabitsAction
import space.be1ski.vibits.feature.habits.presentation.state.HabitsState
import space.be1ski.vibits.feature.habits.presentation.state.getActivityData
import space.be1ski.vibits.feature.habits.presentation.view.components.rememberHabitsConfigTimeline
import space.be1ski.vibits.feature.homescreen.domain.model.AppState
import space.be1ski.vibits.feature.homescreen.domain.model.Screen
import space.be1ski.vibits.feature.homescreen.presentation.AppFeatures
import space.be1ski.vibits.feature.memos.presentation.state.MemosState

@Suppress("LongParameterList")
internal class AppShellState(
  val timeZone: TimeZone,
  val today: LocalDate,
  val dateFormatter: DateFormatter,
  val habitsTimeline: List<HabitsConfigEntry>,
  val todayHabits: TodayHabits,
  val activityRange: ActivityRange,
  val feedListState: LazyListState,
  val callbacks: ScaffoldCallbacks,
)

@Composable
internal fun rememberAppShellState(
  features: AppFeatures,
  appState: AppState,
  memosState: MemosState,
  habitsState: HabitsState,
): AppShellState {
  val timeZone = remember { TimeZone.currentSystemDefault() }
  val today = currentLocalDate()
  val dateFormatter = rememberDateFormatter()
  val habitsTimeline = rememberHabitsConfigTimeline(memosState.memos)
  val todayHabits = rememberTodayHabits(habitsTimeline, memosState.memos, timeZone, today)

  val activityRange = activityRangeForAppState(appState)
  val feedListState = rememberLazyListState()
  val scope = rememberCoroutineScope()
  val callbacks =
    rememberScaffoldCallbacks(
      appState = appState,
      activityRange = activityRange,
      onAppAction = features.app::send,
      onHabitsAction = features.habits::send,
      onMemosAction = features.memos::send,
      feedListState = feedListState,
      scope = scope,
      todayHabits = todayHabits,
    )

  PrewarmCacheEffects(features, appState, memosState, habitsState)

  return AppShellState(
    timeZone = timeZone,
    today = today,
    dateFormatter = dateFormatter,
    habitsTimeline = habitsTimeline,
    todayHabits = todayHabits,
    activityRange = activityRange,
    feedListState = feedListState,
    callbacks = callbacks,
  )
}

@Composable
private fun PrewarmCacheEffects(
  features: AppFeatures,
  appState: AppState,
  memosState: MemosState,
  habitsState: HabitsState,
) {
  var prevAppMode by remember { mutableStateOf<AppMode?>(null) }
  var prevMemosRevision by remember { mutableStateOf(0) }

  LaunchedEffect(appState.appMode) {
    if (prevAppMode != null && prevAppMode != appState.appMode) {
      features.habits.send(HabitsAction.Cache.InvalidateAllCache)
      prevMemosRevision = 0
    }
    prevAppMode = appState.appMode
  }

  LaunchedEffect(
    appState.appMode,
    memosState.memosRevision,
    habitsState.needsCacheRefresh,
    habitsState.isInitialLoading,
  ) {
    val revisionChanged = memosState.memosRevision != prevMemosRevision && prevMemosRevision != 0
    val shouldPrewarm =
      !habitsState.isInitialLoading &&
        (habitsState.needsCacheRefresh || habitsState.activityDataCache.isEmpty() || revisionChanged)
    if (memosState.memos.isNotEmpty() && shouldPrewarm) {
      prevMemosRevision = memosState.memosRevision
      features.habits.send(
        HabitsAction.Cache.RequestPrewarmAllRanges(
          memos = memosState.memos,
          appMode = appState.appMode,
        ),
      )
    }
  }
}

@Composable
internal fun rememberSuccessRateIfNeeded(
  appState: AppState,
  habitsTimeline: List<HabitsConfigEntry>,
  activityRange: ActivityRange,
  habitsState: HabitsState,
): Float? {
  val isHabitsScreen = appState.selectedScreen == Screen.HABITS
  val hasHabits = remember(habitsTimeline) { habitsTimeline.lastOrNull()?.habits?.isNotEmpty() == true }
  val shouldCalculate = isHabitsScreen && hasHabits && appState.selectedHabitTag == null
  return if (shouldCalculate) {
    val cachedData =
      habitsState.getActivityData(
        activityRange,
        ActivityMode.HABITS,
        appState.appMode,
      )
    cachedData?.successRate?.rate
  } else {
    null
  }
}
