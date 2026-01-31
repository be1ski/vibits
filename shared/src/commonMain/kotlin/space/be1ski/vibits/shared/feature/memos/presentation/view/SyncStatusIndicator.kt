package space.be1ski.vibits.shared.feature.memos.presentation.view

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import space.be1ski.vibits.shared.core.ui.theme.Indent
import space.be1ski.vibits.shared.feature.sync.domain.model.SyncStatus
import space.be1ski.vibits.shared.generated.Res
import space.be1ski.vibits.shared.generated.action_sync
import space.be1ski.vibits.shared.generated.format_pending_sync
import space.be1ski.vibits.shared.generated.msg_syncing

@Composable
fun SyncStatusIndicator(
  syncStatus: SyncStatus,
  isSyncing: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val backgroundColor by animateColorAsState(
    targetValue =
      when {
        isSyncing -> MaterialTheme.colorScheme.primaryContainer
        syncStatus.hasFailedOperations -> MaterialTheme.colorScheme.errorContainer
        syncStatus.hasPendingOperations -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
      },
  )

  val contentColor =
    when {
      isSyncing -> MaterialTheme.colorScheme.onPrimaryContainer
      syncStatus.hasFailedOperations -> MaterialTheme.colorScheme.onErrorContainer
      syncStatus.hasPendingOperations -> MaterialTheme.colorScheme.onSecondaryContainer
      else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

  Row(
    modifier =
      modifier
        .clip(RoundedCornerShape(Indent.Medium))
        .background(backgroundColor)
        .clickable(enabled = !isSyncing, onClick = onClick)
        .padding(horizontal = Indent.Medium, vertical = Indent.Small),
    horizontalArrangement = Arrangement.spacedBy(Indent.Small),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    if (isSyncing) {
      CircularProgressIndicator(
        modifier = Modifier.size(16.dp),
        strokeWidth = 2.dp,
        color = contentColor,
      )
      Text(
        text = stringResource(Res.string.msg_syncing),
        style = MaterialTheme.typography.labelMedium,
        color = contentColor,
      )
    } else if (syncStatus.hasPendingOperations) {
      Icon(
        imageVector = Icons.Default.CloudUpload,
        contentDescription = null,
        modifier = Modifier.size(16.dp),
        tint = contentColor,
      )
      Text(
        text = stringResource(Res.string.format_pending_sync, syncStatus.pendingCount),
        style = MaterialTheme.typography.labelMedium,
        color = contentColor,
      )
    } else if (syncStatus.hasFailedOperations) {
      Icon(
        imageVector = Icons.Default.CloudOff,
        contentDescription = null,
        modifier = Modifier.size(16.dp),
        tint = contentColor,
      )
      Text(
        text = "${syncStatus.failedCount} failed",
        style = MaterialTheme.typography.labelMedium,
        color = contentColor,
      )
    } else {
      Icon(
        imageVector = Icons.Default.CloudDone,
        contentDescription = null,
        modifier = Modifier.size(16.dp),
        tint = contentColor,
      )
      Text(
        text = stringResource(Res.string.action_sync),
        style = MaterialTheme.typography.labelMedium,
        color = contentColor,
      )
    }
  }
}
