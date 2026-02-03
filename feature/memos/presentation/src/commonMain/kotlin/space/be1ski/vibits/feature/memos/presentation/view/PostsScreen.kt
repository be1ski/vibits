package space.be1ski.vibits.feature.memos.presentation.view
import androidx.compose.runtime.Composable
import space.be1ski.vibits.core.platform.mode.AppMode
import space.be1ski.vibits.core.ui.date.DateFormatter
import space.be1ski.vibits.feature.habits.domain.model.ActivityMode
import space.be1ski.vibits.feature.habits.domain.model.ActivityRange
import space.be1ski.vibits.feature.habits.presentation.action.HabitsAction
import space.be1ski.vibits.feature.habits.presentation.state.HabitsState
import space.be1ski.vibits.feature.habits.presentation.view.StatsScreen
import space.be1ski.vibits.feature.habits.presentation.view.StatsScreenState
import space.be1ski.vibits.feature.memos.domain.model.Memo

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
