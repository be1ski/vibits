package space.be1ski.memos.shared.presentation.screen

import androidx.compose.runtime.Composable
import space.be1ski.memos.shared.domain.model.memo.Memo
import space.be1ski.memos.shared.presentation.components.ActivityMode
import space.be1ski.memos.shared.presentation.components.ActivityRange
import space.be1ski.memos.shared.presentation.screen.StatsScreenActions
import space.be1ski.memos.shared.presentation.screen.StatsScreenState

/**
 * Posts stats tab showing activity charts.
 */
@Composable
fun PostsScreen(
  memos: List<Memo>,
  years: List<Int>,
  range: ActivityRange,
  onRangeChange: (ActivityRange) -> Unit
) {
  StatsScreen(
    state = StatsScreenState(
      memos = memos,
      years = years,
      range = range,
      activityMode = ActivityMode.Posts,
      useVerticalScroll = true,
      enablePullRefresh = false
    ),
    actions = StatsScreenActions(
      onRangeChange = onRangeChange
    )
  )
}
