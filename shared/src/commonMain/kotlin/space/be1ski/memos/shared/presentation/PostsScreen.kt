package space.be1ski.memos.shared.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import space.be1ski.memos.shared.domain.model.Memo
import space.be1ski.memos.shared.presentation.components.ActivityMode
import space.be1ski.memos.shared.presentation.components.ActivityRange

/**
 * Posts tab showing activity stats and raw memos list.
 */
@OptIn(androidx.compose.material.ExperimentalMaterialApi::class)
@Composable
fun PostsScreen(
  memos: List<Memo>,
  years: List<Int>,
  range: ActivityRange,
  onRangeChange: (ActivityRange) -> Unit,
  isRefreshing: Boolean = false,
  onRefresh: () -> Unit = {},
  enablePullRefresh: Boolean = true
) {
  val timeZone = TimeZone.currentSystemDefault()
  val pullRefreshState = rememberPullRefreshState(isRefreshing, onRefresh)
  val containerModifier = if (enablePullRefresh) {
    Modifier.pullRefresh(pullRefreshState)
  } else {
    Modifier
  }
  Box(
    modifier = Modifier
      .fillMaxSize()
      .then(containerModifier)
  ) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
      item {
        StatsScreen(
          memos = memos,
          years = years,
          range = range,
          activityMode = ActivityMode.Posts,
          onRangeChange = onRangeChange,
          onEditDailyMemo = { _, _ -> },
          onDeleteDailyMemo = {},
          onCreateDailyMemo = {},
          useVerticalScroll = false,
          isRefreshing = isRefreshing,
          onRefresh = onRefresh,
          enablePullRefresh = false
        )
      }
      items(memos) { memo ->
        Card(modifier = Modifier.fillMaxWidth()) {
          Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            val dateLabel = memoDateLabel(memo, timeZone)
            if (dateLabel.isNotBlank()) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(dateLabel, style = MaterialTheme.typography.labelSmall)
              }
            }
            Text(memo.content, style = MaterialTheme.typography.bodyMedium)
          }
        }
      }
    }
    if (enablePullRefresh) {
      PullRefreshIndicator(
        refreshing = isRefreshing,
        state = pullRefreshState,
        modifier = Modifier.align(Alignment.TopCenter)
      )
    }
  }
}

private fun memoDateLabel(memo: Memo, timeZone: TimeZone): String {
  val instant = memo.updateTime ?: memo.createTime ?: return ""
  val date = instant.toLocalDateTime(timeZone).date
  return date.toString()
}
