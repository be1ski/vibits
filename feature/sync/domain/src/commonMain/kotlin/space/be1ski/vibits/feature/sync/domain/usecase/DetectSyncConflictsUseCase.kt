package space.be1ski.vibits.feature.sync.domain.usecase

import space.be1ski.vibits.core.logging.Log
import space.be1ski.vibits.feature.memos.domain.model.Memo
import space.be1ski.vibits.feature.sync.domain.SyncLogTags
import space.be1ski.vibits.feature.sync.domain.model.ConflictType
import space.be1ski.vibits.feature.sync.domain.model.SyncConflict
import space.be1ski.vibits.feature.sync.domain.model.SyncOperation
import space.be1ski.vibits.feature.sync.domain.model.SyncOperationType
import space.be1ski.vibits.feature.sync.domain.model.TempMemoName

private val TAG = SyncLogTags.SYNC_ENGINE

object DetectSyncConflictsUseCase {
  operator fun invoke(
    pendingOperations: List<SyncOperation>,
    localMemos: List<Memo>,
    serverMemos: List<Memo>,
  ): List<SyncConflict> {
    val serverMemosByName = serverMemos.associateBy { it.name }
    val localMemosByName = localMemos.associateBy { it.name }

    return pendingOperations.mapNotNull { operation ->
      val memoName = operation.memoName ?: return@mapNotNull null
      detectOperationConflict(operation, memoName, serverMemosByName, localMemosByName)
    }
  }

  private fun detectOperationConflict(
    operation: SyncOperation,
    memoName: String,
    serverMemosByName: Map<String, Memo>,
    localMemosByName: Map<String, Memo>,
  ): SyncConflict? =
    when (operation.type) {
      SyncOperationType.CREATE -> detectCreateConflict(operation, memoName, serverMemosByName, localMemosByName)
      SyncOperationType.UPDATE -> detectUpdateConflict(operation, memoName, serverMemosByName, localMemosByName)
      SyncOperationType.DELETE -> detectDeleteConflict(operation, memoName, serverMemosByName)
    }

  private fun detectCreateConflict(
    operation: SyncOperation,
    memoName: String,
    serverMemosByName: Map<String, Memo>,
    localMemosByName: Map<String, Memo>,
  ): SyncConflict? {
    if (TempMemoName.isTemporary(memoName)) return null
    return serverMemosByName[memoName]?.let { serverMemo ->
      Log.d(TAG, "CREATE conflict: '$memoName' already exists on server (BOTH_MODIFIED)")
      SyncConflict(
        operation = operation,
        localMemo = localMemosByName[memoName],
        serverMemo = serverMemo,
        conflictType = ConflictType.BOTH_MODIFIED,
      )
    }
  }

  private fun detectUpdateConflict(
    operation: SyncOperation,
    memoName: String,
    serverMemosByName: Map<String, Memo>,
    localMemosByName: Map<String, Memo>,
  ): SyncConflict? {
    val serverMemo = serverMemosByName[memoName]
    val localMemo = localMemosByName[memoName]

    return when {
      serverMemo == null -> {
        Log.d(TAG, "UPDATE conflict: '$memoName' deleted on server but modified locally")
        SyncConflict(
          operation = operation,
          localMemo = localMemo,
          serverMemo = null,
          conflictType = ConflictType.DELETED_ON_SERVER,
        )
      }
      isServerNewerThanOperation(serverMemo, operation, localMemo) -> {
        Log.d(
          TAG,
          "UPDATE conflict: '$memoName' server newer (server=${serverMemo.updateTime}, op=${operation.createdAt})",
        )
        SyncConflict(
          operation = operation,
          localMemo = localMemo,
          serverMemo = serverMemo,
          conflictType = ConflictType.SERVER_NEWER,
        )
      }
      else -> null
    }
  }

  private fun isServerNewerThanOperation(
    serverMemo: Memo,
    operation: SyncOperation,
    localMemo: Memo?,
  ): Boolean {
    val serverUpdateTime = serverMemo.updateTime ?: return false
    return localMemo != null && serverUpdateTime > operation.createdAt
  }

  private fun detectDeleteConflict(
    operation: SyncOperation,
    memoName: String,
    serverMemosByName: Map<String, Memo>,
  ): SyncConflict? =
    serverMemosByName[memoName]
      ?.takeIf { it.updateTime?.let { time -> time > operation.createdAt } == true }
      ?.let { serverMemo ->
        Log.d(
          TAG,
          "DELETE conflict: '$memoName' modified on server after delete (server=${serverMemo.updateTime}, op=${operation.createdAt})",
        )
        SyncConflict(
          operation = operation,
          localMemo = null,
          serverMemo = serverMemo,
          conflictType = ConflictType.SERVER_NEWER,
        )
      }
}
