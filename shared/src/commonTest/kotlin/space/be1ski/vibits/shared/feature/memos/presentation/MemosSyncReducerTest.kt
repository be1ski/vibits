package space.be1ski.vibits.shared.feature.memos.presentation

import space.be1ski.vibits.shared.core.elm.test
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo
import space.be1ski.vibits.shared.feature.memos.presentation.action.MemosAction
import space.be1ski.vibits.shared.feature.memos.presentation.effect.MemosEffect
import space.be1ski.vibits.shared.feature.memos.presentation.reducer.memosReducer
import space.be1ski.vibits.shared.feature.memos.presentation.state.MemosState
import space.be1ski.vibits.shared.feature.sync.domain.model.ConflictType
import space.be1ski.vibits.shared.feature.sync.domain.model.SyncConflict
import space.be1ski.vibits.shared.feature.sync.domain.model.SyncOperation
import space.be1ski.vibits.shared.feature.sync.domain.model.SyncOperationType
import space.be1ski.vibits.shared.feature.sync.domain.model.SyncStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Instant

class MemosSyncReducerTest {
  private val testMemo =
    Memo(
      name = "memos/1",
      content = "Test content",
      createTime = Instant.fromEpochMilliseconds(1000L),
      updateTime = Instant.fromEpochMilliseconds(2000L),
    )

  private val testOperation =
    SyncOperation(
      id = "op1",
      type = SyncOperationType.CREATE,
      memoName = "memos/1",
      content = "Test content",
    )

  private val testConflict =
    SyncConflict(
      operation = testOperation,
      localMemo = testMemo,
      serverMemo = testMemo.copy(content = "Server content"),
      conflictType = ConflictType.BOTH_MODIFIED,
    )

  // ========== Sync Actions in Online Mode ==========

  @Test
  fun `when StartSync in online mode then starts syncing and emits PerformSync`() =
    memosReducer.test(MemosState(isOfflineMode = false)) {
      send(MemosAction.Sync.StartSync)

      assertState { isSyncing && errorMessage == null }
      assertCommands(MemosEffect.PerformSync)
    }

  @Test
  fun `when SyncCompleted then updates memos and emits LoadSyncStatus`() =
    memosReducer.test(MemosState(isSyncing = true)) {
      send(MemosAction.Sync.SyncCompleted(listOf(testMemo)))

      assertState {
        memos.size == 1 &&
          memos.first().name == "memos/1" &&
          !isSyncing &&
          syncConflicts.isEmpty() &&
          !showConflictDialog &&
          errorMessage == null
      }
      assertCommands(MemosEffect.LoadSyncStatus)
    }

  @Test
  fun `when SyncConflictDetected then shows conflict dialog`() =
    memosReducer.test(MemosState(isSyncing = true)) {
      send(MemosAction.Sync.SyncConflictDetected(listOf(testConflict)))

      assertState {
        !isSyncing &&
          syncConflicts.size == 1 &&
          showConflictDialog
      }
      assertNoEffects()
    }

  @Test
  fun `when SyncFailed then sets error message`() =
    memosReducer.test(MemosState(isSyncing = true)) {
      send(MemosAction.Sync.SyncFailed("Network error"))

      assertState { !isSyncing && errorMessage == "Network error" }
      assertCommands(MemosEffect.LoadSyncStatus)
    }

  @Test
  fun `when SyncStatusUpdated then updates sync status`() {
    val status = SyncStatus(pendingCount = 5, failedCount = 1)
    memosReducer.test(MemosState()) {
      send(MemosAction.Sync.SyncStatusUpdated(status))

      assertState { syncStatus.pendingCount == 5 && syncStatus.failedCount == 1 }
      assertNoEffects()
    }
  }

  @Test
  fun `when ResolveKeepLocal then starts syncing and emits ForceLocalSync`() =
    memosReducer.test(MemosState(syncConflicts = listOf(testConflict))) {
      send(MemosAction.Sync.ResolveKeepLocal)

      assertState { isSyncing }
      assertCommands(MemosEffect.ForceLocalSync)
    }

  @Test
  fun `when ResolveKeepServer then starts syncing and emits ForceServerSync`() =
    memosReducer.test(MemosState(syncConflicts = listOf(testConflict))) {
      send(MemosAction.Sync.ResolveKeepServer)

      assertState { isSyncing }
      assertCommands(MemosEffect.ForceServerSync)
    }

  @Test
  fun `when DismissConflictDialog then hides dialog`() =
    memosReducer.test(MemosState(showConflictDialog = true)) {
      send(MemosAction.Sync.DismissConflictDialog)

      assertState { !showConflictDialog }
      assertNoEffects()
    }

  // ========== Sync Actions in Offline Mode (Should Be No-ops) ==========

  @Test
  fun `when StartSync in offline mode then does nothing`() =
    memosReducer.test(MemosState(isOfflineMode = true)) {
      send(MemosAction.Sync.StartSync)

      assertState { !isSyncing }
      assertNoEffects()
    }

  @Test
  fun `when SyncCompleted in offline mode then does nothing`() =
    memosReducer.test(MemosState(isOfflineMode = true, memos = emptyList())) {
      send(MemosAction.Sync.SyncCompleted(listOf(testMemo)))

      assertState { memos.isEmpty() }
      assertNoEffects()
    }

  @Test
  fun `when SyncConflictDetected in offline mode then does nothing`() =
    memosReducer.test(MemosState(isOfflineMode = true)) {
      send(MemosAction.Sync.SyncConflictDetected(listOf(testConflict)))

      assertState { syncConflicts.isEmpty() && !showConflictDialog }
      assertNoEffects()
    }

  @Test
  fun `when SyncFailed in offline mode then does nothing`() =
    memosReducer.test(MemosState(isOfflineMode = true)) {
      send(MemosAction.Sync.SyncFailed("Network error"))

      assertState { errorMessage == null }
      assertNoEffects()
    }

  @Test
  fun `when SyncStatusUpdated in offline mode then does nothing`() {
    val status = SyncStatus(pendingCount = 5)
    memosReducer.test(MemosState(isOfflineMode = true)) {
      send(MemosAction.Sync.SyncStatusUpdated(status))

      assertState { syncStatus.pendingCount == 0 }
      assertNoEffects()
    }
  }

  @Test
  fun `when ResolveKeepLocal in offline mode then does nothing`() =
    memosReducer.test(MemosState(isOfflineMode = true)) {
      send(MemosAction.Sync.ResolveKeepLocal)

      assertState { !isSyncing }
      assertNoEffects()
    }

  @Test
  fun `when ResolveKeepServer in offline mode then does nothing`() =
    memosReducer.test(MemosState(isOfflineMode = true)) {
      send(MemosAction.Sync.ResolveKeepServer)

      assertState { !isSyncing }
      assertNoEffects()
    }

  @Test
  fun `when DismissConflictDialog in offline mode then does nothing`() =
    memosReducer.test(MemosState(isOfflineMode = true, showConflictDialog = true)) {
      send(MemosAction.Sync.DismissConflictDialog)

      // In offline mode, the state shouldn't change from the reducer
      // But showConflictDialog starts as true, and offline mode skips the action
      assertState { showConflictDialog }
      assertNoEffects()
    }

  // ========== Computed Properties ==========

  @Test
  fun `hasPendingSync returns true when there are pending operations`() {
    val state = MemosState(syncStatus = SyncStatus(pendingCount = 3))
    assertTrue(state.hasPendingSync)
  }

  @Test
  fun `hasPendingSync returns false when no pending operations`() {
    val state = MemosState(syncStatus = SyncStatus(pendingCount = 0))
    assertFalse(state.hasPendingSync)
  }

  @Test
  fun `hasSyncConflicts returns true when there are conflicts`() {
    val state = MemosState(syncConflicts = listOf(testConflict))
    assertTrue(state.hasSyncConflicts)
  }

  @Test
  fun `hasSyncConflicts returns false when no conflicts`() {
    val state = MemosState(syncConflicts = emptyList())
    assertFalse(state.hasSyncConflicts)
  }

  @Test
  fun `when SyncCompleted then increments memosRevision`() {
    val initialState = MemosState(memosRevision = 5, isSyncing = true)
    memosReducer.test(initialState) {
      send(MemosAction.Sync.SyncCompleted(listOf(testMemo)))

      assertState { memosRevision == 6 }
    }
  }
}
