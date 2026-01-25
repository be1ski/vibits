package space.be1ski.vibits.shared.feature.memos.presentation

import space.be1ski.vibits.shared.core.elm.test
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo
import space.be1ski.vibits.shared.feature.memos.domain.model.MemosContent
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
      send(MemosAction.UpdateBaseUrl("https://example.com"))

      assertState { baseUrl == "https://example.com" && errorMessage == null }
      assertNoEffects()
    }

  @Test
  fun `when UpdateToken then updates token and clears error`() =
    memosReducer.test(MemosState(errorMessage = "some error")) {
      send(MemosAction.UpdateToken("secret-token"))

      assertState { token == "secret-token" && errorMessage == null }
      assertNoEffects()
    }

  @Test
  fun `when EditCredentials then sets credentials mode and emits LoadCredentials`() =
    memosReducer.test(MemosState()) {
      send(MemosAction.EditCredentials)

      assertState { credentialsMode && errorMessage == null }
      assertEffects(MemosEffect.LoadCredentials)
    }

  @Test
  fun `when CredentialsLoaded then updates baseUrl and token`() =
    memosReducer.test(MemosState()) {
      send(MemosAction.CredentialsLoaded("https://api.com", "token123"))

      assertState { baseUrl == "https://api.com" && token == "token123" }
      assertNoEffects()
    }

  @Test
  fun `when LoadMemos without credentials then shows error`() =
    memosReducer.test(MemosState(content = MemosContent(baseUrl = "", token = ""))) {
      send(MemosAction.LoadMemos)

      assertState { credentialsMode && errorMessage == "Base URL and token are required." }
      assertNoEffects()
    }

  @Test
  fun `when LoadMemos with credentials then starts loading and emits effects`() =
    memosReducer.test(MemosState(content = MemosContent(baseUrl = "https://api.com", token = "token123"))) {
      send(MemosAction.LoadMemos)

      assertState { isLoading && !credentialsMode && errorMessage == null }
      assertEffectCount(2)
      assertHasEffect<MemosEffect.SaveCredentials>()
      assertHasEffect<MemosEffect.LoadRemoteMemos>()
    }

  @Test
  fun `when LoadCachedMemos then emits LoadCachedMemos effect`() =
    memosReducer.test(MemosState()) {
      send(MemosAction.LoadCachedMemos)

      assertEffects(MemosEffect.LoadCachedMemos)
    }

  @Test
  fun `when CachedMemosLoaded with empty state then updates memos`() =
    memosReducer.test(MemosState()) {
      send(MemosAction.CachedMemosLoaded(listOf(testMemo)))

      assertState { memos.size == 1 && memos.first().name == testMemo.name }
      assertNoEffects()
    }

  @Test
  fun `when CachedMemosLoaded with existing memos then ignores cached`() =
    memosReducer.test(MemosState(content = MemosContent(memos = listOf(testMemo.copy(name = "memos/existing"))))) {
      send(MemosAction.CachedMemosLoaded(listOf(testMemo)))

      assertState { memos.size == 1 && memos.first().name == "memos/existing" }
      assertNoEffects()
    }

  @Test
  fun `when MemosLoaded then updates memos and stops loading`() =
    memosReducer.test(MemosState(isLoading = true, errorMessage = "old error")) {
      send(MemosAction.MemosLoaded(listOf(testMemo)))

      assertState { memos.size == 1 && !isLoading && errorMessage == null }
      assertNoEffects()
    }

  @Test
  fun `when CreateMemo then starts loading and emits CreateMemo effect`() =
    memosReducer.test(MemosState()) {
      send(MemosAction.CreateMemo("New memo content"))

      assertState { isLoading }
      val effect = assertHasEffect<MemosEffect.CreateMemo>()
      assertEquals("New memo content", effect.content)
    }

  @Test
  fun `when UpdateMemo then starts loading and emits UpdateMemo effect`() =
    memosReducer.test(MemosState()) {
      send(MemosAction.UpdateMemo("memos/1", "Updated content"))

      assertState { isLoading }
      val effect = assertHasEffect<MemosEffect.UpdateMemo>()
      assertEquals("memos/1", effect.name)
      assertEquals("Updated content", effect.content)
    }

  @Test
  fun `when DeleteMemo then starts loading and emits DeleteMemo effect`() =
    memosReducer.test(MemosState()) {
      send(MemosAction.DeleteMemo("memos/1"))

      assertState { isLoading }
      val effect = assertHasEffect<MemosEffect.DeleteMemo>()
      assertEquals("memos/1", effect.name)
    }

  @Test
  fun `when MemoCreated then adds memo and stops loading`() =
    memosReducer.test(MemosState(content = MemosContent(memos = listOf(testMemo)), isLoading = true)) {
      send(MemosAction.MemoCreated(Memo(name = "memos/2", content = "New")))

      assertState { memos.size == 2 && !isLoading }
      assertNoEffects()
    }

  @Test
  fun `when MemoUpdated then updates memo in list and stops loading`() =
    memosReducer.test(MemosState(content = MemosContent(memos = listOf(testMemo)), isLoading = true)) {
      send(MemosAction.MemoUpdated(testMemo.copy(content = "Updated content")))

      assertState { memos.size == 1 && memos.first().content == "Updated content" && !isLoading }
      assertNoEffects()
    }

  @Test
  fun `when MemoDeleted then removes memo from list and stops loading`() =
    memosReducer.test(MemosState(content = MemosContent(memos = listOf(testMemo)), isLoading = true)) {
      send(MemosAction.MemoDeleted("memos/1"))

      assertState { memos.isEmpty() && !isLoading }
      assertNoEffects()
    }

  @Test
  fun `when OperationFailed then sets error and stops loading`() =
    memosReducer.test(MemosState(isLoading = true)) {
      send(MemosAction.OperationFailed("Network error"))

      assertState { !isLoading && errorMessage == "Network error" }
      assertNoEffects()
    }

  @Test
  fun `when MemosLoaded then memos are sorted by update time descending`() =
    memosReducer.test(MemosState()) {
      val oldMemo = Memo(name = "memos/old", updateTime = Instant.fromEpochMilliseconds(1000L))
      val newMemo = Memo(name = "memos/new", updateTime = Instant.fromEpochMilliseconds(2000L))

      send(MemosAction.MemosLoaded(listOf(oldMemo, newMemo)))

      assertState { memos.first().name == "memos/new" && memos.last().name == "memos/old" }
    }

  @Test
  fun `when LoadMemos in offline mode then does not emit SaveCredentials`() =
    memosReducer.test(
      MemosState(content = MemosContent(baseUrl = "https://api.com", token = "token", isOfflineMode = true)),
    ) {
      send(MemosAction.LoadMemos)

      assertState { isLoading }
      assertEffectCount(1)
      assertHasEffect<MemosEffect.LoadRemoteMemos>()
    }

  @Test
  fun `when ShowCreateDialog then opens dialog with empty content`() =
    memosReducer.test(MemosState()) {
      send(MemosAction.ShowCreateDialog)

      assertState { showCreateDialog && createDialogContent == "" }
      assertNoEffects()
    }

  @Test
  fun `when UpdateCreateContent then updates create dialog content`() =
    memosReducer.test(MemosState(showCreateDialog = true)) {
      send(MemosAction.UpdateCreateContent("New content"))

      assertState { createDialogContent == "New content" }
      assertNoEffects()
    }

  @Test
  fun `when DismissCreateDialog then closes dialog and clears content`() =
    memosReducer.test(MemosState(showCreateDialog = true, createDialogContent = "Some content")) {
      send(MemosAction.DismissCreateDialog)

      assertState { !showCreateDialog && createDialogContent == "" }
      assertNoEffects()
    }

  @Test
  fun `when ConfirmCreateDialog with content then closes dialog and emits CreateMemo`() =
    memosReducer.test(MemosState(showCreateDialog = true, createDialogContent = "  New memo  ")) {
      send(MemosAction.ConfirmCreateDialog)

      assertState { !showCreateDialog && createDialogContent == "" && isLoading }
      val effect = assertHasEffect<MemosEffect.CreateMemo>()
      assertEquals("New memo", effect.content)
    }

  @Test
  fun `when ConfirmCreateDialog with blank content then does nothing`() =
    memosReducer.test(MemosState(showCreateDialog = true, createDialogContent = "   ")) {
      send(MemosAction.ConfirmCreateDialog)

      assertState { showCreateDialog && createDialogContent == "   " }
      assertNoEffects()
    }

  @Test
  fun `when ShowEditDialog then opens dialog with memo content`() =
    memosReducer.test(MemosState()) {
      send(MemosAction.ShowEditDialog(testMemo))

      assertState { showEditDialog && editDialogContent == testMemo.content && editDialogMemo == testMemo }
      assertNoEffects()
    }

  @Test
  fun `when UpdateEditContent then updates edit dialog content`() =
    memosReducer.test(MemosState(showEditDialog = true, editDialogMemo = testMemo)) {
      send(MemosAction.UpdateEditContent("Updated content"))

      assertState { editDialogContent == "Updated content" }
      assertNoEffects()
    }

  @Test
  fun `when DismissEditDialog then closes dialog and clears state`() =
    memosReducer.test(MemosState(showEditDialog = true, editDialogContent = "Content", editDialogMemo = testMemo)) {
      send(MemosAction.DismissEditDialog)

      assertState { !showEditDialog && editDialogContent == "" && editDialogMemo == null }
      assertNoEffects()
    }

  @Test
  fun `when ConfirmEditDialog with content then closes dialog and emits UpdateMemo`() =
    memosReducer.test(MemosState(showEditDialog = true, editDialogContent = "  Updated  ", editDialogMemo = testMemo)) {
      send(MemosAction.ConfirmEditDialog)

      assertState { !showEditDialog && editDialogContent == "" && editDialogMemo == null && isLoading }
      val effect = assertHasEffect<MemosEffect.UpdateMemo>()
      assertEquals(testMemo.name, effect.name)
      assertEquals("Updated", effect.content)
    }

  @Test
  fun `when ConfirmEditDialog with blank content then does nothing`() =
    memosReducer.test(MemosState(showEditDialog = true, editDialogContent = "   ", editDialogMemo = testMemo)) {
      send(MemosAction.ConfirmEditDialog)

      assertState { showEditDialog }
      assertNoEffects()
    }

  @Test
  fun `when ConfirmEditDialog with null memo then does nothing`() =
    memosReducer.test(MemosState(showEditDialog = true, editDialogContent = "Content", editDialogMemo = null)) {
      send(MemosAction.ConfirmEditDialog)

      assertState { showEditDialog }
      assertNoEffects()
    }
}
