package space.be1ski.memos.shared.feature.memos.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.datetime.Instant
import space.be1ski.memos.shared.feature.memos.domain.model.Memo

class MemosReducerTest {

  private val testMemo = Memo(
    name = "memos/1",
    content = "Test content",
    createTime = Instant.fromEpochMilliseconds(1000L),
    updateTime = Instant.fromEpochMilliseconds(2000L)
  )

  // Credentials input tests

  @Test
  fun `when UpdateBaseUrl then updates baseUrl and clears error`() {
    val state = MemosState(errorMessage = "some error")

    val (newState, effects) = memosReducer(
      MemosAction.UpdateBaseUrl("https://example.com"),
      state
    )

    assertEquals("https://example.com", newState.baseUrl)
    assertNull(newState.errorMessage)
    assertTrue(effects.isEmpty())
  }

  @Test
  fun `when UpdateToken then updates token and clears error`() {
    val state = MemosState(errorMessage = "some error")

    val (newState, effects) = memosReducer(
      MemosAction.UpdateToken("secret-token"),
      state
    )

    assertEquals("secret-token", newState.token)
    assertNull(newState.errorMessage)
    assertTrue(effects.isEmpty())
  }

  @Test
  fun `when EditCredentials then sets credentials mode and emits LoadCredentials`() {
    val (newState, effects) = memosReducer(
      MemosAction.EditCredentials,
      MemosState()
    )

    assertTrue(newState.credentialsMode)
    assertNull(newState.errorMessage)
    assertEquals(1, effects.size)
    assertIs<MemosEffect.LoadCredentials>(effects.first())
  }

  @Test
  fun `when CredentialsLoaded then updates baseUrl and token`() {
    val (newState, effects) = memosReducer(
      MemosAction.CredentialsLoaded("https://api.com", "token123"),
      MemosState()
    )

    assertEquals("https://api.com", newState.baseUrl)
    assertEquals("token123", newState.token)
    assertTrue(effects.isEmpty())
  }

  // Loading tests

  @Test
  fun `when LoadMemos without credentials then shows error`() {
    val state = MemosState(baseUrl = "", token = "")

    val (newState, effects) = memosReducer(MemosAction.LoadMemos, state)

    assertTrue(newState.credentialsMode)
    assertEquals("Base URL and token are required.", newState.errorMessage)
    assertTrue(effects.isEmpty())
  }

  @Test
  fun `when LoadMemos with credentials then starts loading and emits effects`() {
    val state = MemosState(baseUrl = "https://api.com", token = "token123")

    val (newState, effects) = memosReducer(MemosAction.LoadMemos, state)

    assertTrue(newState.isLoading)
    assertFalse(newState.credentialsMode)
    assertNull(newState.errorMessage)
    assertEquals(2, effects.size)
    assertIs<MemosEffect.SaveCredentials>(effects[0])
    assertIs<MemosEffect.LoadRemoteMemos>(effects[1])
  }

  @Test
  fun `when LoadCachedMemos then emits LoadCachedMemos effect`() {
    val (_, effects) = memosReducer(MemosAction.LoadCachedMemos, MemosState())

    assertEquals(1, effects.size)
    assertIs<MemosEffect.LoadCachedMemos>(effects.first())
  }

  @Test
  fun `when CachedMemosLoaded with empty state then updates memos`() {
    val memos = listOf(testMemo)

    val (newState, effects) = memosReducer(
      MemosAction.CachedMemosLoaded(memos),
      MemosState()
    )

    assertEquals(1, newState.memos.size)
    assertEquals(testMemo.name, newState.memos.first().name)
    assertTrue(effects.isEmpty())
  }

  @Test
  fun `when CachedMemosLoaded with existing memos then ignores cached`() {
    val existingMemo = testMemo.copy(name = "memos/existing")
    val state = MemosState(memos = listOf(existingMemo))

    val (newState, effects) = memosReducer(
      MemosAction.CachedMemosLoaded(listOf(testMemo)),
      state
    )

    assertEquals(1, newState.memos.size)
    assertEquals("memos/existing", newState.memos.first().name)
    assertTrue(effects.isEmpty())
  }

  @Test
  fun `when MemosLoaded then updates memos and stops loading`() {
    val memos = listOf(testMemo)
    val state = MemosState(isLoading = true, errorMessage = "old error")

    val (newState, effects) = memosReducer(
      MemosAction.MemosLoaded(memos),
      state
    )

    assertEquals(1, newState.memos.size)
    assertFalse(newState.isLoading)
    assertNull(newState.errorMessage)
    assertTrue(effects.isEmpty())
  }

  // CRUD tests

  @Test
  fun `when CreateMemo then starts loading and emits CreateMemo effect`() {
    val (newState, effects) = memosReducer(
      MemosAction.CreateMemo("New memo content"),
      MemosState()
    )

    assertTrue(newState.isLoading)
    assertEquals(1, effects.size)
    val effect = effects.first()
    assertIs<MemosEffect.CreateMemo>(effect)
    assertEquals("New memo content", effect.content)
  }

  @Test
  fun `when UpdateMemo then starts loading and emits UpdateMemo effect`() {
    val (newState, effects) = memosReducer(
      MemosAction.UpdateMemo("memos/1", "Updated content"),
      MemosState()
    )

    assertTrue(newState.isLoading)
    assertEquals(1, effects.size)
    val effect = effects.first()
    assertIs<MemosEffect.UpdateMemo>(effect)
    assertEquals("memos/1", effect.name)
    assertEquals("Updated content", effect.content)
  }

  @Test
  fun `when DeleteMemo then starts loading and emits DeleteMemo effect`() {
    val (newState, effects) = memosReducer(
      MemosAction.DeleteMemo("memos/1"),
      MemosState()
    )

    assertTrue(newState.isLoading)
    assertEquals(1, effects.size)
    val effect = effects.first()
    assertIs<MemosEffect.DeleteMemo>(effect)
    assertEquals("memos/1", effect.name)
  }

  // CRUD response tests

  @Test
  fun `when MemoCreated then adds memo and stops loading`() {
    val state = MemosState(isLoading = true, memos = listOf(testMemo))
    val newMemo = Memo(name = "memos/2", content = "New")

    val (newState, effects) = memosReducer(
      MemosAction.MemoCreated(newMemo),
      state
    )

    assertEquals(2, newState.memos.size)
    assertFalse(newState.isLoading)
    assertTrue(effects.isEmpty())
  }

  @Test
  fun `when MemoUpdated then updates memo in list and stops loading`() {
    val state = MemosState(isLoading = true, memos = listOf(testMemo))
    val updatedMemo = testMemo.copy(content = "Updated content")

    val (newState, effects) = memosReducer(
      MemosAction.MemoUpdated(updatedMemo),
      state
    )

    assertEquals(1, newState.memos.size)
    assertEquals("Updated content", newState.memos.first().content)
    assertFalse(newState.isLoading)
    assertTrue(effects.isEmpty())
  }

  @Test
  fun `when MemoDeleted then removes memo from list and stops loading`() {
    val state = MemosState(isLoading = true, memos = listOf(testMemo))

    val (newState, effects) = memosReducer(
      MemosAction.MemoDeleted("memos/1"),
      state
    )

    assertTrue(newState.memos.isEmpty())
    assertFalse(newState.isLoading)
    assertTrue(effects.isEmpty())
  }

  @Test
  fun `when OperationFailed then sets error and stops loading`() {
    val state = MemosState(isLoading = true)

    val (newState, effects) = memosReducer(
      MemosAction.OperationFailed("Network error"),
      state
    )

    assertFalse(newState.isLoading)
    assertEquals("Network error", newState.errorMessage)
    assertTrue(effects.isEmpty())
  }

  // Sorting tests

  @Test
  fun `memos are sorted by update time descending`() {
    val oldMemo = Memo(
      name = "memos/old",
      updateTime = Instant.fromEpochMilliseconds(1000L)
    )
    val newMemo = Memo(
      name = "memos/new",
      updateTime = Instant.fromEpochMilliseconds(2000L)
    )

    val (newState, _) = memosReducer(
      MemosAction.MemosLoaded(listOf(oldMemo, newMemo)),
      MemosState()
    )

    assertEquals("memos/new", newState.memos.first().name)
    assertEquals("memos/old", newState.memos.last().name)
  }
}
