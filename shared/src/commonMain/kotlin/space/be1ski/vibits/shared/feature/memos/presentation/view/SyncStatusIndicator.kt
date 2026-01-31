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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import space.be1ski.vibits.shared.core.ui.Indent
import space.be1ski.vibits.shared.feature.sync.domain.model.SyncStatus
import space.be1ski.vibits.shared.generated.Res
import space.be1ski.vibits.shared.generated.action_sync
import space.be1ski.vibits.shared.generated.format_pending_sync
import space.be1ski.vibits.shared.generated.msg_syncing

private val IconSize = 16.dp
private val StrokeWidth = 2.dp

@Composable
fun SyncStatusIndicator(
  syncStatus: SyncStatus,
  isSyncing: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val backgroundColor by animateColorAsState(
    targetValue = getSyncBackgroundColor(syncStatus, isSyncing),
  )
  val contentColor = getSyncContentColor(syncStatus, isSyncing)

  Row(
    modifier =
      modifier
        .clip(RoundedCornerShape(Indent.m))
        .background(backgroundColor)
        .clickable(enabled = !isSyncing, onClick = onClick)
        .padding(horizontal = Indent.m, vertical = Indent.s),
    horizontalArrangement = Arrangement.spacedBy(Indent.s),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    SyncStatusContent(syncStatus, isSyncing, contentColor)
  }
}

@Composable
private fun getSyncBackgroundColor(
  syncStatus: SyncStatus,
  isSyncing: Boolean,
): Color =
  when {
    isSyncing -> MaterialTheme.colorScheme.primaryContainer
    syncStatus.hasFailedOperations -> MaterialTheme.colorScheme.errorContainer
    syncStatus.hasPendingOperations -> MaterialTheme.colorScheme.secondaryContainer
    else -> MaterialTheme.colorScheme.surfaceVariant
  }

@Composable
private fun getSyncContentColor(
  syncStatus: SyncStatus,
  isSyncing: Boolean,
): Color =
  when {
    isSyncing -> MaterialTheme.colorScheme.onPrimaryContainer
    syncStatus.hasFailedOperations -> MaterialTheme.colorScheme.onErrorContainer
    syncStatus.hasPendingOperations -> MaterialTheme.colorScheme.onSecondaryContainer
    else -> MaterialTheme.colorScheme.onSurfaceVariant
  }

@Composable
private fun SyncStatusContent(
  syncStatus: SyncStatus,
  isSyncing: Boolean,
  contentColor: Color,
) {
  when {
    isSyncing -> SyncingContent(contentColor)
    syncStatus.hasPendingOperations -> PendingContent(syncStatus.pendingCount, contentColor)
    syncStatus.hasFailedOperations -> FailedContent(syncStatus.failedCount, contentColor)
    else -> SyncedContent(contentColor)
  }
}

@Composable
private fun SyncingContent(contentColor: Color) {
  CircularProgressIndicator(
    modifier = Modifier.size(IconSize),
    strokeWidth = StrokeWidth,
    color = contentColor,
  )
  Text(
    text = stringResource(Res.string.msg_syncing),
    style = MaterialTheme.typography.labelMedium,
    color = contentColor,
  )
}

@Composable
private fun PendingContent(
  pendingCount: Int,
  contentColor: Color,
) {
  Icon(
    imageVector = Icons.Default.CloudUpload,
    contentDescription = null,
    modifier = Modifier.size(IconSize),
    tint = contentColor,
  )
  Text(
    text = stringResource(Res.string.format_pending_sync, pendingCount),
    style = MaterialTheme.typography.labelMedium,
    color = contentColor,
  )
}

@Composable
private fun FailedContent(
  failedCount: Int,
  contentColor: Color,
) {
  Icon(
    imageVector = Icons.Default.CloudOff,
    contentDescription = null,
    modifier = Modifier.size(IconSize),
    tint = contentColor,
  )
  Text(
    text = "$failedCount failed",
    style = MaterialTheme.typography.labelMedium,
    color = contentColor,
  )
}

@Composable
private fun SyncedContent(contentColor: Color) {
  Icon(
    imageVector = Icons.Default.CloudDone,
    contentDescription = null,
    modifier = Modifier.size(IconSize),
    tint = contentColor,
  )
  Text(
    text = stringResource(Res.string.action_sync),
    style = MaterialTheme.typography.labelMedium,
    color = contentColor,
  )
}
