package space.be1ski.vibits.feature.main.view

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import space.be1ski.vibits.feature.habits.domain.model.ActivityRange
import space.be1ski.vibits.feature.habits.domain.usecase.NavigateActivityRangeUseCase
import space.be1ski.vibits.feature.habits.presentation.action.HabitsAction
import space.be1ski.vibits.feature.main.domain.model.AppState
import space.be1ski.vibits.feature.main.domain.model.Screen
import space.be1ski.vibits.feature.main.presentation.action.AppAction
import space.be1ski.vibits.feature.memos.presentation.action.MemosAction
import space.be1ski.vibits.feature.settings.domain.model.TimeRangeTab

@Suppress("LongParameterList")
internal class ScaffoldCallbacks(
  val onClearSelection: () -> Unit,
  val onFeedScrollToTop: () -> Unit,
  val onShowCreateMemoDialog: () -> Unit,
  val onOpenTodayEditor: () -> Unit,
  val onTabChange: (TimeRangeTab) -> Unit,
  val onNavigateBack: () -> Unit,
  val onNavigateForward: () -> Unit,
)

@Suppress("LongMethod")
@Composable
internal fun rememberScaffoldCallbacks(
  appState: AppState,
  activityRange: ActivityRange,
  onAppAction: (AppAction) -> Unit,
  onHabitsAction: (HabitsAction) -> Unit,
  onMemosAction: (MemosAction) -> Unit,
  feedListState: LazyListState,
  scope: CoroutineScope,
  todayData: TodayData,
): ScaffoldCallbacks {
  val onClearSelection =
    remember(onHabitsAction) {
      { onHabitsAction(HabitsAction.Selection.ClearSelection) }
    }
  val onFeedScrollToTop =
    remember(feedListState, scope) {
      {
        scope.launch { feedListState.animateScrollToItem(0) }
        Unit
      }
    }
  val onShowCreateMemoDialog =
    remember(onMemosAction) {
      { onMemosAction(MemosAction.CreateDialog.ShowCreateDialog) }
    }
  val onOpenTodayEditor =
    remember(onHabitsAction, todayData) {
      {
        todayData.day?.let { onHabitsAction(HabitsAction.Editor.OpenEditor(day = it, config = todayData.config)) }
        Unit
      }
    }
  val onTabChange =
    remember(onHabitsAction, onAppAction, appState) {
      { newTab: TimeRangeTab ->
        onHabitsAction(HabitsAction.Selection.ClearSelection)
        when (appState.selectedScreen) {
          Screen.HABITS -> onAppAction(AppAction.TimeRange.ChangeHabitsTab(appState.habitsTimeRangeTab, newTab))
          Screen.STATS -> onAppAction(AppAction.TimeRange.ChangePostsTab(appState.postsTimeRangeTab, newTab))
          Screen.FEED -> {}
        }
      }
    }
  val onNavigateBack =
    remember(onHabitsAction, onAppAction, activityRange) {
      {
        val newRange = NavigateActivityRangeUseCase(activityRange, -1)
        onHabitsAction(HabitsAction.Selection.ClearSelection)
        onAppAction(AppAction.TimeRange.SetActivityRange(newRange))
      }
    }
  val onNavigateForward =
    remember(onHabitsAction, onAppAction, activityRange) {
      {
        val newRange = NavigateActivityRangeUseCase(activityRange, 1)
        onHabitsAction(HabitsAction.Selection.ClearSelection)
        onAppAction(AppAction.TimeRange.SetActivityRange(newRange))
      }
    }
  return ScaffoldCallbacks(
    onClearSelection = onClearSelection,
    onFeedScrollToTop = onFeedScrollToTop,
    onShowCreateMemoDialog = onShowCreateMemoDialog,
    onOpenTodayEditor = onOpenTodayEditor,
    onTabChange = onTabChange,
    onNavigateBack = onNavigateBack,
    onNavigateForward = onNavigateForward,
  )
}
