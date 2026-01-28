package space.be1ski.vibits.shared.feature.memos.view

import androidx.compose.runtime.Composable
import space.be1ski.vibits.shared.app.domain.model.ActivityMode
import space.be1ski.vibits.shared.app.domain.model.ActivityRange
import space.be1ski.vibits.shared.core.ui.date.DateFormatter
import space.be1ski.vibits.shared.feature.habits.view.StatsScreen
import space.be1ski.vibits.shared.feature.habits.view.StatsScreenState
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo

/**
 * Posts stats tab showing activity charts.
 */
@Composable
fun PostsScreen(
  memos: List<Memo>,
  range: ActivityRange,
  demoMode: Boolean,
  dateFormatter: DateFormatter,
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
    dateFormatter = dateFormatter,
    onPostsListExpandedChange = onPostsListExpandedChange,
  )
}
