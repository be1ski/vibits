package space.be1ski.vibits.shared.feature.memos.presentation.view

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import space.be1ski.vibits.shared.feature.sync.domain.model.SyncStatus

private val DotSize = 10.dp

@Suppress("MagicNumber")
private object SyncColors {
  val Synced = Color(0xFF4CAF50) // Material Green 500
  val Syncing = Color(0xFF4CAF50) // Material Green 500 (animated)
  val Error = Color(0xFFF44336) // Material Red 500
  val Pending = Color(0xFFFF9800) // Material Orange 500
}

/**
 * Minimal sync status dot indicator inspired by Anytype.
 * Shows sync status with a simple colored dot:
 * - Green: synced
 * - Green (pulsing): syncing in progress
 * - Orange: pending operations
 * - Red: error
 */
@Composable
fun SyncDot(
  syncStatus: SyncStatus,
  isSyncing: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val dotColor by animateColorAsState(
    targetValue = getDotColor(syncStatus, isSyncing),
    animationSpec = tween(durationMillis = 300),
  )

  val alpha =
    if (isSyncing) {
      val infiniteTransition = rememberInfiniteTransition()
      val animatedAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec =
          infiniteRepeatable(
            animation = tween(durationMillis = 800),
            repeatMode = RepeatMode.Reverse,
          ),
      )
      animatedAlpha
    } else {
      1f
    }

  Box(
    modifier =
      modifier
        .size(DotSize)
        .clip(CircleShape)
        .alpha(alpha)
        .background(dotColor)
        .clickable(onClick = onClick),
  )
}

private fun getDotColor(
  syncStatus: SyncStatus,
  isSyncing: Boolean,
): Color =
  when {
    isSyncing -> SyncColors.Syncing
    syncStatus.hasFailedOperations -> SyncColors.Error
    syncStatus.hasPendingOperations -> SyncColors.Pending
    else -> SyncColors.Synced
  }
