package space.be1ski.vibits.shared.feature.sync.domain.model

import kotlin.time.Instant

/**
 * Current sync status for the app.
 */
data class SyncStatus(
  val pendingCount: Int = 0,
  val failedCount: Int = 0,
  val lastSyncTime: Instant? = null,
  val isSyncing: Boolean = false,
  val hasConflict: Boolean = false,
) {
  val hasPendingOperations: Boolean get() = pendingCount > 0
  val hasFailedOperations: Boolean get() = failedCount > 0
  val needsSync: Boolean get() = hasPendingOperations || hasFailedOperations
}
