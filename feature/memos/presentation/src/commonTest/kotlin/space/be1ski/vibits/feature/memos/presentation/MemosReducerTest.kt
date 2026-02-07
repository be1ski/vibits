package space.be1ski.vibits.feature.memos.presentation

import space.be1ski.vibits.core.elm.test.test
import space.be1ski.vibits.core.platform.mode.AppMode
import space.be1ski.vibits.feature.memos.domain.model.Memo
import space.be1ski.vibits.feature.memos.domain.model.PostFilter
import space.be1ski.vibits.feature.memos.presentation.action.MemosAction
import space.be1ski.vibits.feature.memos.presentation.effect.MemosEffect
import space.be1ski.vibits.feature.memos.presentation.reducer.memosReducer
import space.be1ski.vibits.feature.memos.presentation.state.MemosState
import space.be1ski.vibits.feature.sync.domain.model.ConflictType
import space.be1ski.vibits.feature.sync.domain.model.SyncConflict
import space.be1ski.vibits.feature.sync.domain.model.SyncOperation
import space.be1ski.vibits.feature.sync.domain.model.SyncOperationType
import space.be1ski.vibits.feature.sync.domain.model.SyncStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class MemosReducerTest {
  private val testMemo =
    Memo(
      name = "memos/1",
      content = "Test content",
      createTime = Instant.fromEpochMilliseconds(1000L),
      updateTime = Instant.fromEpochMilliseconds(2000L),
    )

  @Test
  fun `when UpdateBaseUrl then updates baseUrl and clears error`() =
    memosReducer.test(MemosState(errorMessage = "some error")) {
      send(MemosAction.Credentials.UpdateBaseUrl("https://example.com"))

      assertState { baseUrl == "https://example.com" && errorMessage == null }
      assertNoEffects()
    }

  @Test
  fun `when UpdateToken then updates token and clears error`() =
    memosReducer.test(MemosState(errorMessage = "some error")) {
      send(MemosAction.Credentials.UpdateToken("secret-token"))

      assertState { token == "secret-token" && errorMessage == null }
      assertNoEffects()
    }

  @Test
  fun `when EditCredentials then sets credentials mode and emits LoadCredentials`() =
    memosReducer.test(MemosState()) {
      send(MemosAction.Credentials.EditCredentials)

      assertState { credentialsMode && errorMessage == null }
      assertCommands(MemosEffect.LoadCredentials)
    }

  @Test
  fun `when CredentialsLoaded then updates baseUrl and token`() =
    memosReducer.test(MemosState()) {
      send(MemosAction.Credentials.CredentialsLoaded("https://api.com", "token123"))

      assertState { baseUrl == "https://api.com" && token == "token123" }
      assertNoEffects()
    }

  @Test
  fun `when LoadMemos without credentials then shows error`() =
    memosReducer.test(MemosState(baseUrl = "", token = "")) {
      send(MemosAction.Loading.LoadMemos)

      assertState { credentialsMode && errorMessage == null }
      assertNoEffects()
    }

  @Test
  fun `when LoadMemos with credentials then starts loading and emits effects`() =
    memosReducer.test(MemosState(baseUrl = "https://api.com", token = "token123")) {
      send(MemosAction.Loading.LoadMemos)

      assertState { isLoading && !credentialsMode && errorMessage == null }
      assertCommandCount(2)
      assertHasCommand<MemosEffect.SaveCredentials>()
      assertHasCommand<MemosEffect.LoadRemoteMemos>()
    }

  @Test
  fun `when LoadCachedMemos then emits LoadCachedMemos effect`() =
    memosReducer.test(MemosState()) {
      send(MemosAction.Loading.LoadCachedMemos)

      assertCommands(MemosEffect.LoadCachedMemos)
    }

  @Test
  fun `when CachedMemosLoaded with empty state then updates memos`() =
    memosReducer.test(MemosState()) {
      send(MemosAction.Loading.CachedMemosLoaded(listOf(testMemo)))

      assertState { memos.size == 1 && memos.first().name == testMemo.name && initialDataLoaded }
      assertNoEffects()
    }

  @Test
  fun `when CachedMemosLoaded with existing memos then ignores cached`() =
    memosReducer.test(MemosState(memos = listOf(testMemo.copy(name = "memos/existing")))) {
      send(MemosAction.Loading.CachedMemosLoaded(listOf(testMemo)))

      assertState { memos.size == 1 && memos.first().name == "memos/existing" }
      assertNoEffects()
    }

  @Test
  fun `when CachedMemosLoaded with empty cache in offline mode then marks as loaded`() =
    memosReducer.test(MemosState(isOfflineMode = true)) {
      send(MemosAction.Loading.CachedMemosLoaded(emptyList()))

      assertState { memos.isEmpty() && initialDataLoaded }
      assertNoEffects()
    }

  @Test
  fun `when CachedMemosLoaded with empty cache in online mode then loads from server`() =
    memosReducer.test(MemosState(isOfflineMode = false)) {
      send(MemosAction.Loading.CachedMemosLoaded(emptyList()))

      assertState { memos.isEmpty() && !initialDataLoaded && isLoading }
      assertCommands(MemosEffect.LoadRemoteMemos)
    }

  @Test
  fun `when ResetForModeChange to OFFLINE then clears memos and sets isOfflineMode true`() =
    memosReducer.test(MemosState(memos = listOf(testMemo), initialDataLoaded = true, isLoading = true)) {
      send(MemosAction.Loading.ResetForModeChange(AppMode.OFFLINE))

      assertState { memos.isEmpty() && !initialDataLoaded && !isLoading && isOfflineMode }
      assertNoEffects()
    }

  @Test
  fun `when ResetForModeChange to ONLINE then clears memos and sets isOfflineMode false`() =
    memosReducer.test(MemosState(memos = listOf(testMemo), initialDataLoaded = true, isLoading = true, isOfflineMode = true)) {
      send(MemosAction.Loading.ResetForModeChange(AppMode.ONLINE))

      assertState { memos.isEmpty() && !initialDataLoaded && !isLoading && !isOfflineMode }
      assertNoEffects()
    }

  @Test
  fun `when MemosLoaded then updates memos and stops loading`() =
    memosReducer.test(MemosState(isLoading = true, errorMessage = "old error")) {
      send(MemosAction.Loading.MemosLoaded(listOf(testMemo)))

      assertState { memos.size == 1 && !isLoading && errorMessage == null && initialDataLoaded }
      assertNoEffects()
    }

  @Test
  fun `when CreateMemo then starts loading and emits CreateMemo effect`() =
    memosReducer.test(MemosState()) {
      send(MemosAction.Crud.CreateMemo("New memo content"))

      assertState { isLoading }
      val effect = assertHasCommand<MemosEffect.CreateMemo>()
      assertEquals("New memo content", effect.content)
    }

  @Test
  fun `when UpdateMemo then starts loading and emits UpdateMemo effect`() =
    memosReducer.test(MemosState()) {
      send(MemosAction.Crud.UpdateMemo("memos/1", "Updated content"))

      assertState { isLoading }
      val effect = assertHasCommand<MemosEffect.UpdateMemo>()
      assertEquals("memos/1", effect.name)
      assertEquals("Updated content", effect.content)
    }

  @Test
  fun `when DeleteMemo then starts loading and emits DeleteMemo effect`() =
    memosReducer.test(MemosState()) {
      send(MemosAction.Crud.DeleteMemo("memos/1"))

      assertState { isLoading }
      val effect = assertHasCommand<MemosEffect.DeleteMemo>()
      assertEquals("memos/1", effect.name)
    }

  @Test
  fun `when MemoCreated then adds memo stops loading and triggers sync`() =
    memosReducer.test(MemosState(isLoading = true, memos = listOf(testMemo))) {
      send(MemosAction.Crud.MemoCreated(Memo(name = "memos/2", content = "New")))

      assertState { memos.size == 2 && !isLoading }
      assertCommands(MemosEffect.PerformSync)
    }

  @Test
  fun `when MemoUpdated then updates memo stops loading and triggers sync`() =
    memosReducer.test(MemosState(isLoading = true, memos = listOf(testMemo))) {
      send(MemosAction.Crud.MemoUpdated(testMemo.copy(content = "Updated content")))

      assertState { memos.size == 1 && memos.first().content == "Updated content" && !isLoading }
      assertCommands(MemosEffect.PerformSync)
    }

  @Test
  fun `when MemoDeleted then removes memo stops loading and triggers sync`() =
    memosReducer.test(MemosState(isLoading = true, memos = listOf(testMemo))) {
      send(MemosAction.Crud.MemoDeleted("memos/1"))

      assertState { memos.isEmpty() && !isLoading }
      assertCommands(MemosEffect.PerformSync)
    }

  @Test
  fun `when MemoCreated in offline mode then does not trigger sync`() =
    memosReducer.test(MemosState(isLoading = true, memos = listOf(testMemo), isOfflineMode = true)) {
      send(MemosAction.Crud.MemoCreated(Memo(name = "memos/2", content = "New")))

      assertState { memos.size == 2 && !isLoading }
      assertNoEffects()
    }

  @Test
  fun `when OperationFailed then sets error and stops loading`() =
    memosReducer.test(MemosState(isLoading = true)) {
      send(MemosAction.Crud.OperationFailed("Network error"))

      assertState { !isLoading && errorMessage == "Network error" }
      assertNoEffects()
    }

  @Test
  fun `when MemosLoaded then memos are sorted by update time descending`() =
    memosReducer.test(MemosState()) {
      val oldMemo = Memo(name = "memos/old", updateTime = Instant.fromEpochMilliseconds(1000L))
      val newMemo = Memo(name = "memos/new", updateTime = Instant.fromEpochMilliseconds(2000L))

      send(MemosAction.Loading.MemosLoaded(listOf(oldMemo, newMemo)))

      assertState { memos.first().name == "memos/new" && memos.last().name == "memos/old" }
    }

  @Test
  fun `when MemosLoaded with tracking posts then sorted by tracked date not creation time`() =
    memosReducer.test(MemosState()) {
      val trackingJan15 =
        Memo(
          name = "memos/jan15",
          content = "#daily/2026-01-15\n\n#habits/exercise",
          createTime = Instant.fromEpochMilliseconds(3000L),
        )
      val trackingJan20 =
        Memo(
          name = "memos/jan20",
          content = "#daily/2026-01-20\n\n#habits/reading",
          createTime = Instant.fromEpochMilliseconds(1000L),
        )
      val regularPost =
        Memo(
          name = "memos/regular",
          content = "Just a regular post",
          createTime = Instant.fromEpochMilliseconds(2000L),
        )

      send(MemosAction.Loading.MemosLoaded(listOf(trackingJan15, regularPost, trackingJan20)))

      assertState {
        memos.size == 3 &&
          memos[0].name == "memos/jan20" &&
          memos[1].name == "memos/jan15" &&
          memos[2].name == "memos/regular"
      }
    }

  @Test
  fun `when LoadMemos in offline mode then does not emit SaveCredentials`() =
    memosReducer.test(MemosState(baseUrl = "https://api.com", token = "token", isOfflineMode = true)) {
      send(MemosAction.Loading.LoadMemos)

      assertState { isLoading }
      assertCommandCount(1)
      assertHasCommand<MemosEffect.LoadRemoteMemos>()
    }

  @Test
  fun `when ShowCreateDialog then opens dialog with empty content`() =
    memosReducer.test(MemosState()) {
      send(MemosAction.CreateDialog.ShowCreateDialog)

      assertState { showCreateDialog && createDialogContent == "" }
      assertNoEffects()
    }

  @Test
  fun `when UpdateCreateContent then updates create dialog content`() =
    memosReducer.test(MemosState(showCreateDialog = true)) {
      send(MemosAction.CreateDialog.UpdateCreateContent("New content"))

      assertState { createDialogContent == "New content" }
      assertNoEffects()
    }

  @Test
  fun `when DismissCreateDialog then closes dialog and clears content`() =
    memosReducer.test(MemosState(showCreateDialog = true, createDialogContent = "Some content")) {
      send(MemosAction.CreateDialog.DismissCreateDialog)

      assertState { !showCreateDialog && createDialogContent == "" }
      assertNoEffects()
    }

  @Test
  fun `when ConfirmCreateDialog with content then closes dialog and emits CreateMemo`() =
    memosReducer.test(MemosState(showCreateDialog = true, createDialogContent = "  New memo  ")) {
      send(MemosAction.CreateDialog.ConfirmCreateDialog)

      assertState { !showCreateDialog && createDialogContent == "" && isLoading }
      val effect = assertHasCommand<MemosEffect.CreateMemo>()
      assertEquals("New memo", effect.content)
    }

  @Test
  fun `when ConfirmCreateDialog with blank content then does nothing`() =
    memosReducer.test(MemosState(showCreateDialog = true, createDialogContent = "   ")) {
      send(MemosAction.CreateDialog.ConfirmCreateDialog)

      assertState { showCreateDialog && createDialogContent == "   " }
      assertNoEffects()
    }

  @Test
  fun `when ShowEditDialog then opens dialog with memo content`() =
    memosReducer.test(MemosState()) {
      send(MemosAction.EditDialog.ShowEditDialog(testMemo))

      assertState { showEditDialog && editDialogContent == testMemo.content && editDialogMemo == testMemo }
      assertNoEffects()
    }

  @Test
  fun `when UpdateEditContent then updates edit dialog content`() =
    memosReducer.test(MemosState(showEditDialog = true, editDialogMemo = testMemo)) {
      send(MemosAction.EditDialog.UpdateEditContent("Updated content"))

      assertState { editDialogContent == "Updated content" }
      assertNoEffects()
    }

  @Test
  fun `when DismissEditDialog then closes dialog and clears state`() =
    memosReducer.test(MemosState(showEditDialog = true, editDialogContent = "Content", editDialogMemo = testMemo)) {
      send(MemosAction.EditDialog.DismissEditDialog)

      assertState { !showEditDialog && editDialogContent == "" && editDialogMemo == null }
      assertNoEffects()
    }

  @Test
  fun `when ConfirmEditDialog with content then closes dialog and emits UpdateMemo`() =
    memosReducer.test(MemosState(showEditDialog = true, editDialogContent = "  Updated  ", editDialogMemo = testMemo)) {
      send(MemosAction.EditDialog.ConfirmEditDialog)

      assertState { !showEditDialog && editDialogContent == "" && editDialogMemo == null && isLoading }
      val effect = assertHasCommand<MemosEffect.UpdateMemo>()
      assertEquals(testMemo.name, effect.name)
      assertEquals("Updated", effect.content)
    }

  @Test
  fun `when ConfirmEditDialog with blank content then does nothing`() =
    memosReducer.test(MemosState(showEditDialog = true, editDialogContent = "   ", editDialogMemo = testMemo)) {
      send(MemosAction.EditDialog.ConfirmEditDialog)

      assertState { showEditDialog }
      assertNoEffects()
    }

  @Test
  fun `when ConfirmEditDialog with null memo then does nothing`() =
    memosReducer.test(MemosState(showEditDialog = true, editDialogContent = "Content", editDialogMemo = null)) {
      send(MemosAction.EditDialog.ConfirmEditDialog)

      assertState { showEditDialog }
      assertNoEffects()
    }

  // ============ Loading Reducer - Additional Tests ============

  @Test
  fun `when RefreshMemos then emits RefreshMemos effect`() =
    memosReducer.test(MemosState()) {
      send(MemosAction.Loading.RefreshMemos)

      assertCommands(MemosEffect.RefreshMemos)
    }

  @Test
  fun `when ChangePostFilter then updates activePostFilter`() =
    memosReducer.test(MemosState(activePostFilter = PostFilter.ALL)) {
      send(MemosAction.Loading.ChangePostFilter(PostFilter.HABIT_TRACKING))

      assertState { activePostFilter == PostFilter.HABIT_TRACKING }
      assertNoEffects()
    }

  @Test
  fun `when ResetForModeChange to DEMO then clears memos and sets isOfflineMode true`() =
    memosReducer.test(MemosState(memos = listOf(testMemo), initialDataLoaded = true, isLoading = true)) {
      send(MemosAction.Loading.ResetForModeChange(AppMode.DEMO))

      assertState { memos.isEmpty() && !initialDataLoaded && !isLoading && isOfflineMode }
      assertNoEffects()
    }

  // ============ Crud Reducer - Additional Tests ============

  @Test
  fun `when MemoUpdated in offline mode then does not trigger sync`() =
    memosReducer.test(MemosState(isLoading = true, memos = listOf(testMemo), isOfflineMode = true)) {
      send(MemosAction.Crud.MemoUpdated(testMemo.copy(content = "Updated content")))

      assertState { memos.size == 1 && memos.first().content == "Updated content" && !isLoading }
      assertNoEffects()
    }

  @Test
  fun `when MemoDeleted in offline mode then does not trigger sync`() =
    memosReducer.test(MemosState(isLoading = true, memos = listOf(testMemo), isOfflineMode = true)) {
      send(MemosAction.Crud.MemoDeleted("memos/1"))

      assertState { memos.isEmpty() && !isLoading }
      assertNoEffects()
    }

  // ============ Sync Reducer Tests ============

  @Test
  fun `when StartSync in online mode then sets isSyncing and emits PerformSync`() =
    memosReducer.test(MemosState(isOfflineMode = false, errorMessage = "old error")) {
      send(MemosAction.Sync.StartSync)

      assertState { isSyncing && errorMessage == null }
      assertCommands(MemosEffect.PerformSync)
    }

  @Test
  fun `when StartSync in offline mode then does nothing`() =
    memosReducer.test(MemosState(isOfflineMode = true)) {
      send(MemosAction.Sync.StartSync)

      assertState { !isSyncing }
      assertNoEffects()
    }

  @Test
  fun `when SyncCompleted in online mode then updates memos and clears sync state`() {
    val syncedMemos = listOf(testMemo.copy(name = "memos/synced"))
    memosReducer.test(
      MemosState(
        isOfflineMode = false,
        isSyncing = true,
        syncConflicts =
          listOf(
            SyncConflict(
              operation = SyncOperation(id = "op1", type = SyncOperationType.CREATE, memoName = "memos/1", content = "content"),
              localMemo = testMemo,
              serverMemo = null,
              conflictType = ConflictType.SERVER_NEWER,
            ),
          ),
        showConflictDialog = true,
        errorMessage = "old error",
      ),
    ) {
      send(MemosAction.Sync.SyncCompleted(syncedMemos))

      assertState {
        memos.size == 1 &&
          memos.first().name == "memos/synced" &&
          !isSyncing &&
          syncConflicts.isEmpty() &&
          !showConflictDialog &&
          errorMessage == null
      }
      assertCommands(MemosEffect.LoadSyncStatus)
    }
  }

  @Test
  fun `when SyncCompleted in offline mode then does nothing`() =
    memosReducer.test(MemosState(isOfflineMode = true, isSyncing = true)) {
      send(MemosAction.Sync.SyncCompleted(listOf(testMemo)))

      assertState { isSyncing }
      assertNoEffects()
    }

  @Test
  fun `when SyncConflictDetected in online mode then updates conflicts and shows dialog`() {
    val conflicts =
      listOf(
        SyncConflict(
          operation = SyncOperation(id = "op1", type = SyncOperationType.UPDATE, memoName = "memos/1", content = "content"),
          localMemo = testMemo,
          serverMemo = testMemo.copy(content = "Server content"),
          conflictType = ConflictType.BOTH_MODIFIED,
        ),
      )
    memosReducer.test(MemosState(isOfflineMode = false, isSyncing = true)) {
      send(MemosAction.Sync.SyncConflictDetected(conflicts))

      assertState {
        !isSyncing &&
          syncConflicts.size == 1 &&
          showConflictDialog
      }
      assertNoEffects()
    }
  }

  @Test
  fun `when SyncConflictDetected in offline mode then does nothing`() =
    memosReducer.test(MemosState(isOfflineMode = true, isSyncing = true)) {
      send(MemosAction.Sync.SyncConflictDetected(emptyList()))

      assertState { isSyncing && !showConflictDialog }
      assertNoEffects()
    }

  @Test
  fun `when SyncFailed in online mode then sets error and stops syncing`() =
    memosReducer.test(MemosState(isOfflineMode = false, isSyncing = true)) {
      send(MemosAction.Sync.SyncFailed("Network error"))

      assertState { !isSyncing && errorMessage == "Network error" }
      assertCommands(MemosEffect.LoadSyncStatus)
    }

  @Test
  fun `when SyncFailed in offline mode then does nothing`() =
    memosReducer.test(MemosState(isOfflineMode = true, isSyncing = true)) {
      send(MemosAction.Sync.SyncFailed("Network error"))

      assertState { isSyncing && errorMessage == null }
      assertNoEffects()
    }

  @Test
  fun `when SyncStatusUpdated in online mode then updates syncStatus`() {
    val newStatus = SyncStatus(pendingCount = 5, failedCount = 1)
    memosReducer.test(MemosState(isOfflineMode = false)) {
      send(MemosAction.Sync.SyncStatusUpdated(newStatus))

      assertState { syncStatus.pendingCount == 5 && syncStatus.failedCount == 1 }
      assertNoEffects()
    }
  }

  @Test
  fun `when SyncStatusUpdated in offline mode then does nothing`() {
    val newStatus = SyncStatus(pendingCount = 5)
    memosReducer.test(MemosState(isOfflineMode = true, syncStatus = SyncStatus())) {
      send(MemosAction.Sync.SyncStatusUpdated(newStatus))

      assertState { syncStatus.pendingCount == 0 }
      assertNoEffects()
    }
  }

  @Test
  fun `when ResolveKeepLocal in online mode then starts syncing and emits ForceLocalSync`() =
    memosReducer.test(MemosState(isOfflineMode = false)) {
      send(MemosAction.Sync.ResolveKeepLocal)

      assertState { isSyncing }
      assertCommands(MemosEffect.ForceLocalSync)
    }

  @Test
  fun `when ResolveKeepLocal in offline mode then does nothing`() =
    memosReducer.test(MemosState(isOfflineMode = true)) {
      send(MemosAction.Sync.ResolveKeepLocal)

      assertState { !isSyncing }
      assertNoEffects()
    }

  @Test
  fun `when ResolveKeepServer in online mode then starts syncing and emits ForceServerSync`() =
    memosReducer.test(MemosState(isOfflineMode = false)) {
      send(MemosAction.Sync.ResolveKeepServer)

      assertState { isSyncing }
      assertCommands(MemosEffect.ForceServerSync)
    }

  @Test
  fun `when ResolveKeepServer in offline mode then does nothing`() =
    memosReducer.test(MemosState(isOfflineMode = true)) {
      send(MemosAction.Sync.ResolveKeepServer)

      assertState { !isSyncing }
      assertNoEffects()
    }

  @Test
  fun `when DismissConflictDialog in online mode then hides dialog`() =
    memosReducer.test(MemosState(isOfflineMode = false, showConflictDialog = true)) {
      send(MemosAction.Sync.DismissConflictDialog)

      assertState { !showConflictDialog }
      assertNoEffects()
    }

  @Test
  fun `when DismissConflictDialog in offline mode then does nothing`() =
    memosReducer.test(MemosState(isOfflineMode = true, showConflictDialog = true)) {
      send(MemosAction.Sync.DismissConflictDialog)

      assertState { showConflictDialog }
      assertNoEffects()
    }

  @Test
  fun `when ShowSyncLogDialog in online mode then shows dialog`() =
    memosReducer.test(MemosState(isOfflineMode = false)) {
      send(MemosAction.Sync.ShowSyncLogDialog)

      assertState { showSyncLogDialog }
      assertNoEffects()
    }

  @Test
  fun `when ShowSyncLogDialog in offline mode then does nothing`() =
    memosReducer.test(MemosState(isOfflineMode = true, showSyncLogDialog = false)) {
      send(MemosAction.Sync.ShowSyncLogDialog)

      assertState { !showSyncLogDialog }
      assertNoEffects()
    }

  @Test
  fun `when DismissSyncLogDialog in online mode then hides dialog`() =
    memosReducer.test(MemosState(isOfflineMode = false, showSyncLogDialog = true)) {
      send(MemosAction.Sync.DismissSyncLogDialog)

      assertState { !showSyncLogDialog }
      assertNoEffects()
    }

  @Test
  fun `when DismissSyncLogDialog in offline mode then does nothing`() =
    memosReducer.test(MemosState(isOfflineMode = true, showSyncLogDialog = true)) {
      send(MemosAction.Sync.DismissSyncLogDialog)

      assertState { showSyncLogDialog }
      assertNoEffects()
    }
}
