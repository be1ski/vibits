package space.be1ski.vibits.feature.sync.domain.model

import space.be1ski.vibits.feature.memos.domain.model.Memo

/**
 * Represents a sync conflict between local and server data.
 */
data class SyncConflict(
  val operation: SyncOperation,
  val localMemo: Memo?,
  val serverMemo: Memo?,
  val conflictType: ConflictType,
)

/**
 * Type of conflict detected during sync.
 */
enum class ConflictType {
  /** Server has a newer version of the memo. */
  SERVER_NEWER,

  /** Memo was deleted on server but modified locally. */
  DELETED_ON_SERVER,

  /** Memo was modified on both server and locally. */
  BOTH_MODIFIED,
}

/**
 * User's choice for resolving a sync conflict.
 */
enum class ConflictResolution {
  /** Keep local changes and overwrite server. */
  KEEP_LOCAL,

  /** Keep server data and discard local changes. */
  KEEP_SERVER,
}
