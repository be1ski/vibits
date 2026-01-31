package space.be1ski.vibits.shared.feature.memos.presentation
import space.be1ski.vibits.shared.core.elm.test
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo
import space.be1ski.vibits.shared.feature.memos.presentation.action.MemosAction
import space.be1ski.vibits.shared.feature.memos.presentation.effect.MemosEffect
import space.be1ski.vibits.shared.feature.memos.presentation.reducer.memosReducer
import space.be1ski.vibits.shared.feature.memos.presentation.state.MemosState
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode
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

      assertState { credentialsMode && errorMessage == "Base URL and token are required." }
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
}
