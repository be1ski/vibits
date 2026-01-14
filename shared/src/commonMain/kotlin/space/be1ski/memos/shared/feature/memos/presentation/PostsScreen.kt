package space.be1ski.memos.shared.feature.memos.presentation

import androidx.compose.runtime.Composable
import space.be1ski.memos.shared.feature.habits.presentation.StatsScreen
import space.be1ski.memos.shared.feature.habits.presentation.StatsScreenState
import space.be1ski.memos.shared.feature.memos.domain.model.Memo
import space.be1ski.memos.shared.core.ui.ActivityMode
import space.be1ski.memos.shared.core.ui.ActivityRange

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
    )
  )
}
