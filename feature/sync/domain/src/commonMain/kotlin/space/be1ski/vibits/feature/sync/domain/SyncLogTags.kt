package space.be1ski.vibits.feature.sync.domain

/**
 * Log tags used by sync-related components.
 * Centralized here for reuse in logging and log filtering.
 */
object SyncLogTags {
  const val SYNC_ENGINE = "SyncEngine"
  const val SYNC_OPERATION_APPLIER = "SyncOperationApplier"
  const val SYNC_CONFLICT_DETECTOR = "SyncConflictDetector"
  const val OFFLINE_FIRST_MEMOS = "OfflineFirstMemos"
  const val MEMOS_SYNC_EFFECT = "MemosSyncEffect"
  const val MODE_AWARE_REPO = "ModeAwareRepo"
  const val MEMOS_REPOSITORY = "MemosRepository"
  const val MEMOS_API = "MemosApi"

  /** All sync-related tags for log filtering. */
  val allTags =
    setOf(
      SYNC_ENGINE,
      SYNC_OPERATION_APPLIER,
      SYNC_CONFLICT_DETECTOR,
      OFFLINE_FIRST_MEMOS,
      MEMOS_SYNC_EFFECT,
      MODE_AWARE_REPO,
      MEMOS_REPOSITORY,
      MEMOS_API,
    )
}
