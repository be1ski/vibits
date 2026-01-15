package space.be1ski.vibits.shared.feature.memos.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo
import space.be1ski.vibits.shared.core.ui.DEMO_PLACEHOLDER_CONTENT
import space.be1ski.vibits.shared.core.ui.Indent

/**
 * Feed tab showing the raw memos list.
 */
@Suppress("LongParameterList")
@OptIn(androidx.compose.material.ExperimentalMaterialApi::class)
@Composable
fun FeedScreen(
  memos: List<Memo>,
  isRefreshing: Boolean = false,
  onRefresh: () -> Unit = {},
  enablePullRefresh: Boolean = true,
  demoMode: Boolean = false,
  onMemoClick: (Memo) -> Unit = {}
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
    LazyColumn(verticalArrangement = Arrangement.spacedBy(Indent.s)) {
      items(memos) { memo ->
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onMemoClick(memo) }
        ) {
          Column(
            modifier = Modifier.padding(Indent.s),
            verticalArrangement = Arrangement.spacedBy(Indent.x2s)
          ) {
            val dateLabel = memoDateLabel(memo, timeZone)
            if (dateLabel.isNotBlank()) {
              Text(dateLabel, style = MaterialTheme.typography.labelSmall)
            }
            val content = if (demoMode) DEMO_PLACEHOLDER_CONTENT else memo.content
            Text(content, style = MaterialTheme.typography.bodyMedium)
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
  val dateTime = instant.toLocalDateTime(timeZone)
  val hour = dateTime.hour.toString().padStart(2, '0')
  val minute = dateTime.minute.toString().padStart(2, '0')
  return "${dateTime.date} $hour:$minute"
}
