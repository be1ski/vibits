package space.be1ski.vibits.feature.memos.presentation.reducer

import space.be1ski.vibits.core.elm.Reducer
import space.be1ski.vibits.core.elm.reducer
import space.be1ski.vibits.feature.memos.presentation.action.MemosAction
import space.be1ski.vibits.feature.memos.presentation.effect.MemosEffect
import space.be1ski.vibits.feature.memos.presentation.state.MemosState

/**
 * Reducer for sync-related actions.
 * Sync is only enabled in online mode - all sync actions are no-ops in offline/demo modes.
 */
internal val syncReducer: Reducer<MemosAction.Sync, MemosState, MemosEffect, Nothing> =
  reducer { action, state ->
    // Skip all sync actions in offline mode (offline or demo)
    if (state.isOfflineMode) {
      return@reducer
    }

    when (action) {
      is MemosAction.Sync.StartSync -> {
        state { state.copy(isSyncing = true, errorMessage = null) }
        command(MemosEffect.PerformSync)
      }

      is MemosAction.Sync.SyncCompleted -> {
        state {
          state.copy(
            memos = action.memos,
            memosRevision = state.memosRevision + 1,
            isSyncing = false,
            syncConflicts = emptyList(),
            showConflictDialog = false,
            errorMessage = null,
          )
        }
        command(MemosEffect.LoadSyncStatus)
      }

      is MemosAction.Sync.SyncConflictDetected -> {
        state {
          state.copy(
            isSyncing = false,
            syncConflicts = action.conflicts,
            showConflictDialog = true,
          )
        }
      }

      is MemosAction.Sync.SyncFailed -> {
        state { state.copy(isSyncing = false, errorMessage = action.error) }
        command(MemosEffect.LoadSyncStatus)
      }

      is MemosAction.Sync.SyncStatusUpdated -> {
        state { state.copy(syncStatus = action.status) }
      }

      is MemosAction.Sync.ResolveKeepLocal -> {
        state { state.copy(isSyncing = true) }
        command(MemosEffect.ForceLocalSync)
      }

      is MemosAction.Sync.ResolveKeepServer -> {
        state { state.copy(isSyncing = true) }
        command(MemosEffect.ForceServerSync)
      }

      is MemosAction.Sync.DismissConflictDialog -> {
        state { state.copy(showConflictDialog = false) }
      }

      is MemosAction.Sync.ShowSyncLogDialog -> {
        state { state.copy(showSyncLogDialog = true) }
      }

      is MemosAction.Sync.DismissSyncLogDialog -> {
        state { state.copy(showSyncLogDialog = false) }
      }
    }
  }
