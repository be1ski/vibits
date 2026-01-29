package space.be1ski.vibits.shared.feature.memos.presentation.view
import androidx.compose.runtime.Composable
import space.be1ski.vibits.shared.app.domain.model.ActivityMode
import space.be1ski.vibits.shared.app.domain.model.ActivityRange
import space.be1ski.vibits.shared.core.ui.date.DateFormatter
import space.be1ski.vibits.shared.feature.habits.presentation.action.HabitsAction
import space.be1ski.vibits.shared.feature.habits.presentation.state.HabitsState
import space.be1ski.vibits.shared.feature.habits.presentation.view.StatsScreen
import space.be1ski.vibits.shared.feature.habits.presentation.view.StatsScreenState
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo
import space.be1ski.vibits.shared.feature.memos.presentation.action.MemosAction
import space.be1ski.vibits.shared.feature.memos.presentation.state.MemosState
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode

/**
 * Posts stats tab showing activity charts.
 */
@Composable
fun PostsScreen(
  memos: List<Memo>,
  range: ActivityRange,
  appMode: AppMode,
  demoMode: Boolean,
  dateFormatter: DateFormatter,
  habitsState: HabitsState = HabitsState(),
  onHabitsAction: (HabitsAction) -> Unit = {},
  postsListExpanded: Boolean = false,
  onPostsListExpandedChange: (Boolean) -> Unit = {},
) {
  StatsScreen(
    state =
      StatsScreenState(
        memos = memos,
        range = range,
        activityMode = ActivityMode.POSTS,
        useVerticalScroll = true,
        enablePullRefresh = false,
        demoMode = demoMode,
        postsListExpanded = postsListExpanded,
      ),
    appMode = appMode,
    dateFormatter = dateFormatter,
    habitsState = habitsState,
    onHabitsAction = onHabitsAction,
    onPostsListExpandedChange = onPostsListExpandedChange,
  )
}
