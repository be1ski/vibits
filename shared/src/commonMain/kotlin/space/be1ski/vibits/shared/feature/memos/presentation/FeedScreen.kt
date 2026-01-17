package space.be1ski.vibits.shared.feature.memos.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import org.jetbrains.compose.resources.stringResource
import space.be1ski.vibits.shared.Res
import space.be1ski.vibits.shared.action_cancel
import space.be1ski.vibits.shared.action_delete
import space.be1ski.vibits.shared.title_delete_memo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import space.be1ski.vibits.shared.core.platform.DateFormatter
import space.be1ski.vibits.shared.core.platform.LocalDateFormatter
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo
import space.be1ski.vibits.shared.core.ui.Indent

/**
 * Feed tab showing the raw memos list.
 */
@Suppress("LongParameterList", "LongMethod")
@OptIn(androidx.compose.material.ExperimentalMaterialApi::class)
@Composable
fun FeedScreen(
  memos: List<Memo>,
  isRefreshing: Boolean = false,
  onRefresh: () -> Unit = {},
  enablePullRefresh: Boolean = true,
  onMemoClick: (Memo) -> Unit = {},
  onDeleteMemo: ((Memo) -> Unit)? = null
) {
  var memoToDelete by remember { mutableStateOf<Memo?>(null) }
  val timeZone = TimeZone.currentSystemDefault()
  val formatter = LocalDateFormatter.current
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
          Row(
            modifier = Modifier.padding(start = Indent.s, top = Indent.s, bottom = Indent.s, end = Indent.xs),
            verticalAlignment = Alignment.Top
          ) {
            Column(
              modifier = Modifier.weight(1f),
              verticalArrangement = Arrangement.spacedBy(Indent.x2s)
            ) {
              val dateLabel = memoDateLabel(memo, timeZone, formatter)
              if (dateLabel.isNotBlank()) {
                Text(dateLabel, style = MaterialTheme.typography.labelSmall)
              }
              Text(memo.content, style = MaterialTheme.typography.bodyMedium)
            }
            if (onDeleteMemo != null) {
              IconButton(
                onClick = { memoToDelete = memo },
                modifier = Modifier.size(36.dp)
              ) {
                Icon(
                  imageVector = Icons.Filled.Delete,
                  contentDescription = stringResource(Res.string.action_delete),
                  modifier = Modifier.size(18.dp),
                  tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
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

  memoToDelete?.let { memo ->
    AlertDialog(
      onDismissRequest = { memoToDelete = null },
      title = { Text(stringResource(Res.string.title_delete_memo)) },
      confirmButton = {
        TextButton(onClick = {
          onDeleteMemo?.invoke(memo)
          memoToDelete = null
        }) {
          Text(stringResource(Res.string.action_delete))
        }
      },
      dismissButton = {
        TextButton(onClick = { memoToDelete = null }) {
          Text(stringResource(Res.string.action_cancel))
        }
      }
    )
  }
}

private fun memoDateLabel(memo: Memo, timeZone: TimeZone, formatter: DateFormatter): String {
  val instant = memo.updateTime ?: memo.createTime ?: return ""
  return formatter.dateTime(instant.toLocalDateTime(timeZone))
}

