package space.be1ski.memos.shared.presentation.screen

import androidx.compose.runtime.Composable
import space.be1ski.memos.shared.domain.model.memo.Memo
import space.be1ski.memos.shared.presentation.components.ActivityMode
import space.be1ski.memos.shared.presentation.components.ActivityRange

/**
 * Posts stats tab showing activity charts.
 */
@Composable
fun PostsScreen(
  memos: List<Memo>,
  range: ActivityRange,
  demoMode: Boolean
) {
  StatsScreen(
    state = StatsScreenState(
      memos = memos,
      range = range,
      activityMode = ActivityMode.Posts,
      useVerticalScroll = true,
      enablePullRefresh = false,
      demoMode = demoMode
    ),
    actions = StatsScreenActions()
  )
}
