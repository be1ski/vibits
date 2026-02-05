package space.be1ski.vibits.feature.memos.presentation.effect

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import space.be1ski.vibits.feature.homescreen.test.FakeMemosRepository
import space.be1ski.vibits.feature.memos.domain.model.Memo
import space.be1ski.vibits.feature.memos.domain.usecase.LoadCachedMemosUseCase
import space.be1ski.vibits.feature.memos.domain.usecase.LoadMemosUseCase
import space.be1ski.vibits.feature.memos.presentation.action.MemosAction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Clock

class MemosLoadEffectHandlerTest {
  private val testMemo =
    Memo(
      name = "memos/1",
      content = "test content",
      createTime = Clock.System.now(),
      updateTime = Clock.System.now(),
    )

  private fun createHandler(
    cachedMemos: List<Memo> = emptyList(),
    listMemosResult: Result<List<Memo>> = Result.success(emptyList()),
  ): MemosLoadEffectHandler {
    val repository = FakeMemosRepository()
    repository.cachedMemosResult = cachedMemos
    repository.listMemosResult = listMemosResult

    return MemosLoadEffectHandler(
      loadMemos = LoadMemosUseCase(repository),
      loadCachedMemos = LoadCachedMemosUseCase(repository),
    )
  }

  // ========== LoadCachedMemos Tests ==========

  @Test
  fun `when LoadCachedMemos succeeds then emits CachedMemosLoaded`() =
    runTest {
      val handler = createHandler(cachedMemos = listOf(testMemo))

      val actions = handler(MemosEffect.LoadCachedMemos).toList()

      assertEquals(1, actions.size)
      assertIs<MemosAction.Loading.CachedMemosLoaded>(actions[0])
      val action = actions[0] as MemosAction.Loading.CachedMemosLoaded
      assertEquals(1, action.memos.size)
      assertEquals("memos/1", action.memos.first().name)
    }

  @Test
  fun `when LoadCachedMemos returns empty list then emits CachedMemosLoaded with empty list`() =
    runTest {
      val handler = createHandler(cachedMemos = emptyList())

      val actions = handler(MemosEffect.LoadCachedMemos).toList()

      assertEquals(1, actions.size)
      assertIs<MemosAction.Loading.CachedMemosLoaded>(actions[0])
      val action = actions[0] as MemosAction.Loading.CachedMemosLoaded
      assertEquals(0, action.memos.size)
    }

  @Test
  fun `when LoadCachedMemos returns multiple memos then emits all memos`() =
    runTest {
      val memos =
        listOf(
          Memo(name = "memos/1", content = "content 1"),
          Memo(name = "memos/2", content = "content 2"),
          Memo(name = "memos/3", content = "content 3"),
        )
      val handler = createHandler(cachedMemos = memos)

      val actions = handler(MemosEffect.LoadCachedMemos).toList()

      assertEquals(1, actions.size)
      val action = actions[0] as MemosAction.Loading.CachedMemosLoaded
      assertEquals(3, action.memos.size)
    }

  // ========== LoadRemoteMemos Tests ==========

  @Test
  fun `when LoadRemoteMemos succeeds then emits MemosLoaded`() =
    runTest {
      val handler = createHandler(listMemosResult = Result.success(listOf(testMemo)))

      val actions = handler(MemosEffect.LoadRemoteMemos).toList()

      assertEquals(1, actions.size)
      assertIs<MemosAction.Loading.MemosLoaded>(actions[0])
      val action = actions[0] as MemosAction.Loading.MemosLoaded
      assertEquals(1, action.memos.size)
      assertEquals("memos/1", action.memos.first().name)
    }

  @Test
  fun `when LoadRemoteMemos returns empty list then emits MemosLoaded with empty list`() =
    runTest {
      val handler = createHandler(listMemosResult = Result.success(emptyList()))

      val actions = handler(MemosEffect.LoadRemoteMemos).toList()

      assertEquals(1, actions.size)
      assertIs<MemosAction.Loading.MemosLoaded>(actions[0])
      val action = actions[0] as MemosAction.Loading.MemosLoaded
      assertEquals(0, action.memos.size)
    }

  @Test
  fun `when LoadRemoteMemos fails then emits OperationFailed`() =
    runTest {
      val handler = createHandler(listMemosResult = Result.failure(RuntimeException("Network error")))

      val actions = handler(MemosEffect.LoadRemoteMemos).toList()

      assertEquals(1, actions.size)
      assertIs<MemosAction.Crud.OperationFailed>(actions[0])
      val action = actions[0] as MemosAction.Crud.OperationFailed
      assertEquals("Network error", action.error)
    }

  @Test
  fun `when LoadRemoteMemos fails with null message then emits default error message`() =
    runTest {
      val handler = createHandler(listMemosResult = Result.failure(RuntimeException()))

      val actions = handler(MemosEffect.LoadRemoteMemos).toList()

      assertEquals(1, actions.size)
      assertIs<MemosAction.Crud.OperationFailed>(actions[0])
      val action = actions[0] as MemosAction.Crud.OperationFailed
      assertEquals("Failed to load memos", action.error)
    }

  // ========== RefreshMemos Tests ==========

  @Test
  fun `when RefreshMemos succeeds then emits MemosLoaded`() =
    runTest {
      val handler = createHandler(cachedMemos = listOf(testMemo))

      val actions = handler(MemosEffect.RefreshMemos).toList()

      assertEquals(1, actions.size)
      assertIs<MemosAction.Loading.MemosLoaded>(actions[0])
      val action = actions[0] as MemosAction.Loading.MemosLoaded
      assertEquals(1, action.memos.size)
      assertEquals("memos/1", action.memos.first().name)
    }

  @Test
  fun `when RefreshMemos returns empty list then emits MemosLoaded with empty list`() =
    runTest {
      val handler = createHandler(cachedMemos = emptyList())

      val actions = handler(MemosEffect.RefreshMemos).toList()

      assertEquals(1, actions.size)
      assertIs<MemosAction.Loading.MemosLoaded>(actions[0])
      val action = actions[0] as MemosAction.Loading.MemosLoaded
      assertEquals(0, action.memos.size)
    }

  @Test
  fun `when RefreshMemos returns multiple memos then emits all memos`() =
    runTest {
      val memos =
        listOf(
          Memo(name = "memos/1", content = "content 1"),
          Memo(name = "memos/2", content = "content 2"),
        )
      val handler = createHandler(cachedMemos = memos)

      val actions = handler(MemosEffect.RefreshMemos).toList()

      assertEquals(1, actions.size)
      val action = actions[0] as MemosAction.Loading.MemosLoaded
      assertEquals(2, action.memos.size)
    }

  // ========== Effect Routing Tests ==========

  @Test
  fun `when different effects are invoked then correct handlers are called`() =
    runTest {
      val memos = listOf(testMemo)
      val handler = createHandler(cachedMemos = memos, listMemosResult = Result.success(memos))

      val cachedActions = handler(MemosEffect.LoadCachedMemos).toList()
      val remoteActions = handler(MemosEffect.LoadRemoteMemos).toList()
      val refreshActions = handler(MemosEffect.RefreshMemos).toList()

      assertIs<MemosAction.Loading.CachedMemosLoaded>(cachedActions[0])
      assertIs<MemosAction.Loading.MemosLoaded>(remoteActions[0])
      assertIs<MemosAction.Loading.MemosLoaded>(refreshActions[0])
    }
}
