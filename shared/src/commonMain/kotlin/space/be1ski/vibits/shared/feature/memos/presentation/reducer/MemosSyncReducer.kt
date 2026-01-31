package space.be1ski.vibits.shared.feature.memos.presentation.reducer

import space.be1ski.vibits.shared.core.elm.Reducer
import space.be1ski.vibits.shared.core.elm.reducer
import space.be1ski.vibits.shared.feature.memos.presentation.action.MemosAction
import space.be1ski.vibits.shared.feature.memos.presentation.effect.MemosEffect
import space.be1ski.vibits.shared.feature.memos.presentation.state.MemosState

/**
 * Reducer for sync-related actions.
 * Sync is only enabled in online mode - all sync actions are no-ops in offline/demo modes.
 */
internal val syncReducer: Reducer<MemosAction.Sync, MemosState, MemosEffect, Nothing> =
  reducer { action, state ->
    // Skip all sync actions in offline mode (offline or demo)
    if (state.isOfflineMode) {
      return@reducer state to emptySet()
    }

    when (action) {
      is MemosAction.Sync.StartSync -> {
        state.copy(isSyncing = true, errorMessage = null) to setOf(MemosEffect.PerformSync)
      }

      is MemosAction.Sync.SyncCompleted -> {
        state.copy(
          memos = action.memos,
          memosRevision = state.memosRevision + 1,
          isSyncing = false,
          syncConflicts = emptyList(),
          showConflictDialog = false,
          errorMessage = null,
        ) to setOf(MemosEffect.LoadSyncStatus)
      }

      is MemosAction.Sync.SyncConflictDetected -> {
        state.copy(
          isSyncing = false,
          syncConflicts = action.conflicts,
          showConflictDialog = true,
        ) to emptySet()
      }

      is MemosAction.Sync.SyncFailed -> {
        state.copy(
          isSyncing = false,
          errorMessage = action.error,
        ) to setOf(MemosEffect.LoadSyncStatus)
      }

      is MemosAction.Sync.SyncStatusUpdated -> {
        state.copy(syncStatus = action.status) to emptySet()
      }

      is MemosAction.Sync.ResolveKeepLocal -> {
        state.copy(isSyncing = true) to setOf(MemosEffect.ForceLocalSync)
      }

      is MemosAction.Sync.ResolveKeepServer -> {
        state.copy(isSyncing = true) to setOf(MemosEffect.ForceServerSync)
      }

      is MemosAction.Sync.DismissConflictDialog -> {
        state.copy(showConflictDialog = false) to emptySet()
      }
    }
  }
