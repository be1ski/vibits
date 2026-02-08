package space.be1ski.vibits.feature.memos.presentation.effect

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import space.be1ski.vibits.feature.memos.domain.model.Memo
import space.be1ski.vibits.feature.memos.domain.test.FakeMemosRepository
import space.be1ski.vibits.feature.memos.domain.usecase.CreateMemoUseCase
import space.be1ski.vibits.feature.memos.domain.usecase.DeleteMemoUseCase
import space.be1ski.vibits.feature.memos.domain.usecase.UpdateMemoUseCase
import space.be1ski.vibits.feature.memos.presentation.action.MemosAction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.time.Clock

class MemosWriteEffectHandlerTest {
  private val testMemo =
    Memo(
      name = "memos/1",
      content = "test content",
      createTime = Clock.System.now(),
      updateTime = Clock.System.now(),
    )

  private fun createHandler(
    createMemoResult: Result<Memo> = Result.success(testMemo),
    updateMemoResult: Result<Memo> = Result.success(testMemo),
    deleteMemoResult: Result<Unit> = Result.success(Unit),
  ): MemosWriteEffectHandler {
    val repository = FakeMemosRepository()
    repository.createMemoResult = createMemoResult
    repository.updateMemoResult = updateMemoResult
    repository.deleteMemoResult = deleteMemoResult

    return MemosWriteEffectHandler(
      createMemo = CreateMemoUseCase(repository),
      updateMemo = UpdateMemoUseCase(repository),
      deleteMemo = DeleteMemoUseCase(repository),
    )
  }

  // ========== CreateMemo Tests ==========

  @Test
  fun `when CreateMemo succeeds then emits MemoCreated`() =
    runTest {
      val createdMemo = Memo(name = "memos/new", content = "new content")
      val handler = createHandler(createMemoResult = Result.success(createdMemo))

      val actions = handler(MemosEffect.CreateMemo("new content")).toList()

      assertEquals(1, actions.size)
      assertIs<MemosAction.Crud.MemoCreated>(actions[0])
      val action = actions[0] as MemosAction.Crud.MemoCreated
      assertEquals("memos/new", action.memo.name)
      assertEquals("new content", action.memo.content)
    }

  @Test
  fun `when CreateMemo fails then emits OperationFailed with error message`() =
    runTest {
      val handler = createHandler(createMemoResult = Result.failure(RuntimeException("Network error")))

      val actions = handler(MemosEffect.CreateMemo("content")).toList()

      assertEquals(1, actions.size)
      assertIs<MemosAction.Crud.OperationFailed>(actions[0])
      val action = actions[0] as MemosAction.Crud.OperationFailed
      assertEquals("Network error", action.error)
    }

  @Test
  fun `when CreateMemo fails with null message then emits default error message`() =
    runTest {
      val handler = createHandler(createMemoResult = Result.failure(RuntimeException()))

      val actions = handler(MemosEffect.CreateMemo("content")).toList()

      assertEquals(1, actions.size)
      assertIs<MemosAction.Crud.OperationFailed>(actions[0])
      val action = actions[0] as MemosAction.Crud.OperationFailed
      assertEquals("Failed to create memo", action.error)
    }

  // ========== UpdateMemo Tests ==========

  @Test
  fun `when UpdateMemo succeeds then emits MemoUpdated`() =
    runTest {
      val updatedMemo = Memo(name = "memos/1", content = "updated content")
      val handler = createHandler(updateMemoResult = Result.success(updatedMemo))

      val actions = handler(MemosEffect.UpdateMemo("memos/1", "updated content")).toList()

      assertEquals(1, actions.size)
      assertIs<MemosAction.Crud.MemoUpdated>(actions[0])
      val action = actions[0] as MemosAction.Crud.MemoUpdated
      assertEquals("memos/1", action.memo.name)
      assertEquals("updated content", action.memo.content)
    }

  @Test
  fun `when UpdateMemo fails then emits OperationFailed with error message`() =
    runTest {
      val handler = createHandler(updateMemoResult = Result.failure(RuntimeException("Update failed")))

      val actions = handler(MemosEffect.UpdateMemo("memos/1", "content")).toList()

      assertEquals(1, actions.size)
      assertIs<MemosAction.Crud.OperationFailed>(actions[0])
      val action = actions[0] as MemosAction.Crud.OperationFailed
      assertEquals("Update failed", action.error)
    }

  @Test
  fun `when UpdateMemo fails with null message then emits default error message`() =
    runTest {
      val handler = createHandler(updateMemoResult = Result.failure(RuntimeException()))

      val actions = handler(MemosEffect.UpdateMemo("memos/1", "content")).toList()

      assertEquals(1, actions.size)
      assertIs<MemosAction.Crud.OperationFailed>(actions[0])
      val action = actions[0] as MemosAction.Crud.OperationFailed
      assertEquals("Failed to update memo", action.error)
    }

  // ========== DeleteMemo Tests ==========

  @Test
  fun `when DeleteMemo succeeds then emits MemoDeleted with name`() =
    runTest {
      val handler = createHandler(deleteMemoResult = Result.success(Unit))

      val actions = handler(MemosEffect.DeleteMemo("memos/1")).toList()

      assertEquals(1, actions.size)
      assertIs<MemosAction.Crud.MemoDeleted>(actions[0])
      val action = actions[0] as MemosAction.Crud.MemoDeleted
      assertEquals("memos/1", action.name)
    }

  @Test
  fun `when DeleteMemo fails then emits OperationFailed with error message`() =
    runTest {
      val handler = createHandler(deleteMemoResult = Result.failure(RuntimeException("Delete failed")))

      val actions = handler(MemosEffect.DeleteMemo("memos/1")).toList()

      assertEquals(1, actions.size)
      assertIs<MemosAction.Crud.OperationFailed>(actions[0])
      val action = actions[0] as MemosAction.Crud.OperationFailed
      assertEquals("Delete failed", action.error)
    }

  @Test
  fun `when DeleteMemo fails with null message then emits default error message`() =
    runTest {
      val handler = createHandler(deleteMemoResult = Result.failure(RuntimeException()))

      val actions = handler(MemosEffect.DeleteMemo("memos/1")).toList()

      assertEquals(1, actions.size)
      assertIs<MemosAction.Crud.OperationFailed>(actions[0])
      val action = actions[0] as MemosAction.Crud.OperationFailed
      assertEquals("Failed to delete memo", action.error)
    }

  // ========== Cancellation Tests ==========

  @Test
  fun `when CreateMemo cancelled then CancellationException propagates`() =
    runTest {
      val handler = createHandler(createMemoResult = Result.failure(CancellationException("cancelled")))

      assertFailsWith<CancellationException> {
        handler(MemosEffect.CreateMemo("content")).toList()
      }
    }

  @Test
  fun `when UpdateMemo cancelled then CancellationException propagates`() =
    runTest {
      val handler = createHandler(updateMemoResult = Result.failure(CancellationException("cancelled")))

      assertFailsWith<CancellationException> {
        handler(MemosEffect.UpdateMemo("memos/1", "content")).toList()
      }
    }

  @Test
  fun `when DeleteMemo cancelled then CancellationException propagates`() =
    runTest {
      val handler = createHandler(deleteMemoResult = Result.failure(CancellationException("cancelled")))

      assertFailsWith<CancellationException> {
        handler(MemosEffect.DeleteMemo("memos/1")).toList()
      }
    }

  // ========== Effect Routing Tests ==========

  @Test
  fun `when different effects are invoked then correct handlers are called`() =
    runTest {
      val handler =
        createHandler(
          createMemoResult = Result.success(testMemo),
          updateMemoResult = Result.success(testMemo),
          deleteMemoResult = Result.success(Unit),
        )

      val createActions = handler(MemosEffect.CreateMemo("content")).toList()
      val updateActions = handler(MemosEffect.UpdateMemo("memos/1", "content")).toList()
      val deleteActions = handler(MemosEffect.DeleteMemo("memos/1")).toList()

      assertIs<MemosAction.Crud.MemoCreated>(createActions[0])
      assertIs<MemosAction.Crud.MemoUpdated>(updateActions[0])
      assertIs<MemosAction.Crud.MemoDeleted>(deleteActions[0])
    }
}
