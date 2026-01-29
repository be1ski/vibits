package space.be1ski.vibits.shared.feature.memos.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import org.jetbrains.compose.resources.stringResource
import space.be1ski.vibits.shared.core.ui.Indent
import space.be1ski.vibits.shared.core.ui.SegmentedSelector
import space.be1ski.vibits.shared.core.ui.date.DateFormatter
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo
import space.be1ski.vibits.shared.feature.memos.domain.model.PostFilter
import space.be1ski.vibits.shared.feature.memos.domain.model.canDeleteFromFeed
import space.be1ski.vibits.shared.feature.memos.domain.model.isConfigPost
import space.be1ski.vibits.shared.feature.memos.domain.model.isTrackingPost
import space.be1ski.vibits.shared.feature.memos.domain.usecase.FilterMemosByTypeUseCase
import space.be1ski.vibits.shared.generated.Res
import space.be1ski.vibits.shared.generated.action_cancel
import space.be1ski.vibits.shared.generated.action_delete
import space.be1ski.vibits.shared.generated.filter_all
import space.be1ski.vibits.shared.generated.filter_config
import space.be1ski.vibits.shared.generated.filter_habit_tracking
import space.be1ski.vibits.shared.generated.filter_regular
import space.be1ski.vibits.shared.generated.label_post_filter
import space.be1ski.vibits.shared.generated.title_delete_memo

/**
 * Feed tab showing the raw memos list.
 */
@OptIn(androidx.compose.material.ExperimentalMaterialApi::class)
@Composable
fun FeedScreen(
  memos: List<Memo>,
  dateFormatter: DateFormatter,
  activeFilter: PostFilter = PostFilter.ALL,
  onFilterChange: (PostFilter) -> Unit = {},
  isRefreshing: Boolean = false,
  onRefresh: () -> Unit = {},
  enablePullRefresh: Boolean = true,
  onMemoClick: (Memo) -> Unit = {},
  onDeleteMemo: ((Memo) -> Unit)? = null,
  demoMode: Boolean = false,
  listState: LazyListState = rememberLazyListState(),
) {
  var memoToDelete by remember { mutableStateOf<Memo?>(null) }
  val timeZone = TimeZone.currentSystemDefault()
  val pullRefreshState = rememberPullRefreshState(isRefreshing, onRefresh)
  val containerModifier =
    if (enablePullRefresh) {
      Modifier.pullRefresh(pullRefreshState)
    } else {
      Modifier
    }

  val filteredMemos = FilterMemosByTypeUseCase(memos, activeFilter)

  Column(modifier = Modifier.fillMaxSize()) {
    Column(modifier = Modifier.padding(horizontal = Indent.s, vertical = Indent.s)) {
      SegmentedSelector(
        label = stringResource(Res.string.label_post_filter),
        options = listOf(PostFilter.ALL, PostFilter.CONFIG, PostFilter.HABIT_TRACKING, PostFilter.REGULAR),
        selected = activeFilter,
        onSelect = onFilterChange,
        optionLabel = { filter ->
          when (filter) {
            PostFilter.ALL -> stringResource(Res.string.filter_all)
            PostFilter.CONFIG -> stringResource(Res.string.filter_config)
            PostFilter.HABIT_TRACKING -> stringResource(Res.string.filter_habit_tracking)
            PostFilter.REGULAR -> stringResource(Res.string.filter_regular)
          }
        },
      )
    }

    Box(
      modifier =
        Modifier
          .fillMaxSize()
          .then(containerModifier),
    ) {
      LazyColumn(state = listState, verticalArrangement = Arrangement.spacedBy(Indent.s)) {
        items(filteredMemos) { memo ->
          Card(
            modifier =
              Modifier
                .fillMaxWidth()
                .clickable { onMemoClick(memo) },
          ) {
            Row(
              modifier = Modifier.padding(start = 0.dp, top = Indent.s, bottom = Indent.s, end = Indent.xs),
              verticalAlignment = Alignment.Top,
            ) {
              PostTypeIndicator(memo = memo)
              Column(
                modifier = Modifier.weight(1f).padding(start = Indent.s),
                verticalArrangement = Arrangement.spacedBy(Indent.x2s),
              ) {
                MemoCardContent(
                  memo = memo,
                  allMemos = memos,
                  dateFormatter = dateFormatter,
                  timeZone = timeZone,
                  demoMode = demoMode,
                )
              }
              if (onDeleteMemo != null && memo.canDeleteFromFeed) {
                IconButton(
                  onClick = { memoToDelete = memo },
                  modifier = Modifier.size(36.dp),
                ) {
                  Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = stringResource(Res.string.action_delete),
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
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
          modifier = Modifier.align(Alignment.TopCenter),
        )
      }
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
      },
    )
  }
}

@Composable
private fun MemoCardContent(
  memo: Memo,
  allMemos: List<Memo>,
  dateFormatter: DateFormatter,
  timeZone: TimeZone,
  demoMode: Boolean,
) {
  when {
    memo.isConfigPost -> {
      HabitsConfigCard(memo = memo, dateFormatter = dateFormatter, demoMode = demoMode)
    }
    memo.isTrackingPost -> {
      HabitsTrackingCard(memo = memo, allMemos = allMemos, dateFormatter = dateFormatter, demoMode = demoMode)
    }
    else -> {
      val dateLabel = memoDateLabel(memo, timeZone, dateFormatter)
      if (dateLabel.isNotBlank()) {
        Text(dateLabel, style = MaterialTheme.typography.labelSmall)
      }
      Text(memo.content, style = MaterialTheme.typography.bodyMedium)
    }
  }
}

private fun memoDateLabel(
  memo: Memo,
  timeZone: TimeZone,
  formatter: DateFormatter,
): String {
  val instant = memo.createTime ?: return ""
  return formatter.dateTime(instant.toLocalDateTime(timeZone))
}
