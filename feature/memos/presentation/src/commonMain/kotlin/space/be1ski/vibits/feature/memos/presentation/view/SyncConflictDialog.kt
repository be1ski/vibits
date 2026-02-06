package space.be1ski.vibits.feature.memos.presentation.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import space.be1ski.vibits.core.strings.generated.Res
import space.be1ski.vibits.core.strings.generated.action_cancel
import space.be1ski.vibits.core.strings.generated.action_keep_local
import space.be1ski.vibits.core.strings.generated.action_keep_server
import space.be1ski.vibits.core.strings.generated.format_conflicts_detected
import space.be1ski.vibits.core.strings.generated.msg_sync_conflict
import space.be1ski.vibits.core.strings.generated.title_sync_conflict
import space.be1ski.vibits.core.ui.Indent

@Composable
fun SyncConflictDialog(
  conflictCount: Int,
  onKeepLocal: () -> Unit,
  onKeepServer: () -> Unit,
  onDismiss: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = stringResource(Res.string.title_sync_conflict),
        style = MaterialTheme.typography.headlineSmall,
      )
    },
    text = {
      Column {
        Text(
          text = stringResource(Res.string.msg_sync_conflict),
          style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.height(Indent.s))
        Text(
          text = stringResource(Res.string.format_conflicts_detected, conflictCount),
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    },
    confirmButton = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
      ) {
        OutlinedButton(onClick = onDismiss) {
          Text(stringResource(Res.string.action_cancel))
        }
        Spacer(modifier = Modifier.width(Indent.s))
        OutlinedButton(onClick = onKeepServer) {
          Text(stringResource(Res.string.action_keep_server))
        }
        Spacer(modifier = Modifier.width(Indent.s))
        Button(
          onClick = onKeepLocal,
          colors =
            ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.primary,
            ),
        ) {
          Text(stringResource(Res.string.action_keep_local))
        }
      }
    },
  )
}
