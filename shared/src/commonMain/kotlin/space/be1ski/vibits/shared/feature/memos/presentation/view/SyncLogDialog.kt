package space.be1ski.vibits.shared.feature.memos.presentation.view

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.jetbrains.compose.resources.stringResource
import space.be1ski.vibits.shared.core.logging.Log
import space.be1ski.vibits.shared.core.logging.LogEntry
import space.be1ski.vibits.shared.core.ui.LogViewer
import space.be1ski.vibits.shared.feature.sync.domain.SyncLogTags
import space.be1ski.vibits.shared.generated.Res
import space.be1ski.vibits.shared.generated.action_clear
import space.be1ski.vibits.shared.generated.action_close
import space.be1ski.vibits.shared.generated.msg_no_logs
import space.be1ski.vibits.shared.generated.title_sync_logs

/**
 * Dialog showing detailed sync operation logs.
 * Filters the app logs to show only sync-related entries.
 */
@Composable
fun SyncLogDialog(onDismiss: () -> Unit) {
  var syncLogs by remember { mutableStateOf(filterSyncLogs(Log.logs)) }

  AlertDialog(
    onDismissRequest = onDismiss,
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
