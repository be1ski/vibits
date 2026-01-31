package space.be1ski.vibits.shared.feature.sync.domain.model

import kotlin.time.Instant

/**
 * Represents a pending sync operation that needs to be sent to the server.
 */
data class SyncOperation(
  val id: String,
  val type: SyncOperationType,
  val memoName: String? = null,
  val content: String? = null,
  val createdAt: Instant =
    kotlin.time.Clock.System
      .now(),
  val status: SyncOperationStatus = SyncOperationStatus.PENDING,
)

/**
 * Type of sync operation.
 */
enum class SyncOperationType {
  CREATE,
  UPDATE,
  DELETE,
}

/**
 * Status of a sync operation.
 */
enum class SyncOperationStatus {
  PENDING,
  IN_PROGRESS,
  FAILED,
  SYNCED,
}
