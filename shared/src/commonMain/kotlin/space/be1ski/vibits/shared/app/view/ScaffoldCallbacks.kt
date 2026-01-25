package space.be1ski.vibits.shared.app.view

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import space.be1ski.vibits.shared.app.domain.model.ActivityRange
import space.be1ski.vibits.shared.app.domain.model.Screen
import space.be1ski.vibits.shared.app.presentation.AppAction
import space.be1ski.vibits.shared.app.presentation.AppState
import space.be1ski.vibits.shared.feature.habits.domain.usecase.NavigateActivityRangeUseCase
import space.be1ski.vibits.shared.feature.habits.presentation.HabitsAction
import space.be1ski.vibits.shared.feature.memos.presentation.MemosAction
import space.be1ski.vibits.shared.feature.settings.domain.model.TimeRangeTab

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
      { onHabitsAction(HabitsAction.ClearSelection) }
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
      { onMemosAction(MemosAction.ShowCreateDialog) }
    }
  val onOpenTodayEditor =
    remember(onHabitsAction, todayData) {
      {
        todayData.day?.let { onHabitsAction(HabitsAction.OpenEditor(day = it, config = todayData.config)) }
        Unit
      }
    }
  val onTabChange =
    remember(onHabitsAction, onAppAction, appState) {
      { newTab: TimeRangeTab ->
        onHabitsAction(HabitsAction.ClearSelection)
        when (appState.selectedScreen) {
          Screen.HABITS -> onAppAction(AppAction.ChangeHabitsTab(appState.habitsTimeRangeTab, newTab))
          Screen.STATS -> onAppAction(AppAction.ChangePostsTab(appState.postsTimeRangeTab, newTab))
          Screen.FEED -> {}
        }
      }
    }
  val onNavigateBack =
    remember(onHabitsAction, onAppAction, activityRange) {
      {
        val newRange = NavigateActivityRangeUseCase(activityRange, -1)
        onHabitsAction(HabitsAction.ClearSelection)
        onAppAction(AppAction.SetActivityRange(newRange))
      }
    }
  val onNavigateForward =
    remember(onHabitsAction, onAppAction, activityRange) {
      {
        val newRange = NavigateActivityRangeUseCase(activityRange, 1)
        onHabitsAction(HabitsAction.ClearSelection)
        onAppAction(AppAction.SetActivityRange(newRange))
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
