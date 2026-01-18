package space.be1ski.vibits.shared.feature.memos.presentation

import androidx.compose.runtime.Composable
import space.be1ski.vibits.shared.core.ui.ActivityMode
import space.be1ski.vibits.shared.core.ui.ActivityRange
import space.be1ski.vibits.shared.feature.habits.domain.usecase.BuildActivityDataUseCase
import space.be1ski.vibits.shared.feature.habits.domain.usecase.CalculateSuccessRateUseCase
import space.be1ski.vibits.shared.feature.habits.presentation.StatsScreen
import space.be1ski.vibits.shared.feature.habits.presentation.StatsScreenState
import space.be1ski.vibits.shared.feature.habits.presentation.components.ActivityWeekDataCache
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo

/**
 * Posts stats tab showing activity charts.
 */
@Suppress("LongParameterList")
@Composable
fun PostsScreen(
  memos: List<Memo>,
  range: ActivityRange,
  demoMode: Boolean,
  calculateSuccessRate: CalculateSuccessRateUseCase,
  buildActivityDataUseCase: BuildActivityDataUseCase,
  cache: ActivityWeekDataCache,
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
    calculateSuccessRate = calculateSuccessRate,
    buildActivityDataUseCase = buildActivityDataUseCase,
    cache = cache,
    onPostsListExpandedChange = onPostsListExpandedChange,
  )
}
