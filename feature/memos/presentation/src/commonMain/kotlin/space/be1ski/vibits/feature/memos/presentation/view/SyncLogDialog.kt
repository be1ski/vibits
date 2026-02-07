package space.be1ski.vibits.feature.memos.presentation.view

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import org.jetbrains.compose.resources.stringResource
import space.be1ski.vibits.core.strings.generated.Res
import space.be1ski.vibits.core.strings.generated.action_clear
import space.be1ski.vibits.core.strings.generated.action_close
import space.be1ski.vibits.core.strings.generated.msg_no_logs
import space.be1ski.vibits.core.strings.generated.title_sync_logs
import space.be1ski.vibits.core.ui.LogViewer
import space.be1ski.vibits.core.utils.logging.Log
import space.be1ski.vibits.core.utils.logging.LogEntry
import space.be1ski.vibits.feature.sync.domain.SyncLogTags

/**
 * Dialog showing detailed sync operation logs.
 * Filters the app logs to show only sync-related entries.
 */
@Composable
fun SyncLogDialog(onDismiss: () -> Unit) {
  var syncLogs by remember { mutableStateOf(filterSyncLogs(Log.logs)) }

  AlertDialog(
    onDismissRequest = onDismiss,
    modifier = Modifier.testTag(FeedTestTags.SYNC_LOG_DIALOG),
    title = { Text(stringResource(Res.string.title_sync_logs, syncLogs.size)) },
    text = {
      LogViewer(
        logs = syncLogs,
        emptyMessage = stringResource(Res.string.msg_no_logs),
      )
    },
    confirmButton = {
      TextButton(
        onClick = {
          Log.clear()
          syncLogs = emptyList()
        },
      ) {
        Text(stringResource(Res.string.action_clear))
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(stringResource(Res.string.action_close))
      }
    },
  )
}

private fun filterSyncLogs(logs: List<LogEntry>): List<LogEntry> = logs.filter { entry -> entry.tag in SyncLogTags.allTags }
