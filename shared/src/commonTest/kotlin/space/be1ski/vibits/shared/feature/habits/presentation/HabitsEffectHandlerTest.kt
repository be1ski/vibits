package space.be1ski.vibits.shared.feature.habits.presentation

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo
import space.be1ski.vibits.shared.test.FakeMemosRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HabitsEffectHandlerTest {
  @Test
  fun `when CreateMemo effect succeeds then emits MemoCreated`() =
    runTest {
      val expectedMemo = Memo(name = "memos/1", content = "test")
      val repository =
        FakeMemosRepository().apply {
          createMemoResult = Result.success(expectedMemo)
        }
      val handler = createHandler(repository)

      val actions = handler(HabitsEffect.CreateMemo(content = "test")).toList()

      assertEquals(listOf(HabitsAction.MemoCreated(expectedMemo)), actions)
      assertEquals(1, repository.createMemoCalls)
    }

  @Test
  fun `when CreateMemo effect fails then emits MemoOperationFailed`() =
    runTest {
      val repository =
        FakeMemosRepository().apply {
          createMemoResult = Result.failure(Exception("Network error"))
        }
      val handler = createHandler(repository)

      val actions = handler(HabitsEffect.CreateMemo(content = "test")).toList()

      assertEquals(1, actions.size)
      assertTrue(actions[0] is HabitsAction.MemoOperationFailed)
      assertEquals("Network error", (actions[0] as HabitsAction.MemoOperationFailed).error)
    }

  @Test
  fun `when UpdateMemo effect succeeds then emits MemoUpdated`() =
    runTest {
      val expectedMemo = Memo(name = "memos/1", content = "updated")
      val repository =
        FakeMemosRepository().apply {
          updateMemoResult = Result.success(expectedMemo)
        }
      val handler = createHandler(repository)

      val actions =
        handler(
          HabitsEffect.UpdateMemo(name = "memos/1", content = "updated"),
        ).toList()

      assertEquals(listOf(HabitsAction.MemoUpdated(expectedMemo)), actions)
      assertEquals(1, repository.updateMemoCalls)
    }

  @Test
  fun `when UpdateMemo effect fails then emits MemoOperationFailed`() =
    runTest {
      val repository =
        FakeMemosRepository().apply {
          updateMemoResult = Result.failure(Exception("Update failed"))
        }
      val handler = createHandler(repository)

      val actions =
        handler(
          HabitsEffect.UpdateMemo(name = "memos/1", content = "updated"),
        ).toList()

      assertEquals(1, actions.size)
      assertTrue(actions[0] is HabitsAction.MemoOperationFailed)
    }

  @Test
  fun `when DeleteMemo effect succeeds then emits MemoDeleted`() =
    runTest {
      val repository =
        FakeMemosRepository().apply {
          deleteMemoResult = Result.success(Unit)
        }
      val handler = createHandler(repository)

      val actions = handler(HabitsEffect.DeleteMemo(name = "memos/1")).toList()

      assertEquals(listOf(HabitsAction.MemoDeleted("memos/1")), actions)
      assertEquals(1, repository.deleteMemoCalls)
    }

  @Test
  fun `when DeleteMemo effect fails then emits MemoOperationFailed`() =
    runTest {
      val repository =
        FakeMemosRepository().apply {
          deleteMemoResult = Result.failure(Exception("Delete failed"))
        }
      val handler = createHandler(repository)

      val actions = handler(HabitsEffect.DeleteMemo(name = "memos/1")).toList()

      assertEquals(1, actions.size)
      assertTrue(actions[0] is HabitsAction.MemoOperationFailed)
    }

  @Test
  fun `when RefreshMemos effect then calls onRefresh callback`() =
    runTest {
      var refreshCalled = false
      val handler = createHandler(onRefresh = { refreshCalled = true })

      handler(HabitsEffect.RefreshMemos).toList()

      assertTrue(refreshCalled)
    }

  private fun createHandler(
    repository: FakeMemosRepository = FakeMemosRepository(),
    onRefresh: () -> Unit = {},
  ): HabitsEffectHandler {
    val buildActivityDataUseCase =
      space.be1ski.vibits.shared.feature.habits.domain.usecase.BuildActivityDataUseCase(
        buildDayDataUseCase =
          space.be1ski.vibits.shared.feature.habits.domain.usecase
            .BuildDayDataUseCase(),
      )
    val calculateSuccessRateUseCase =
      space.be1ski.vibits.shared.feature.habits.domain.usecase
        .CalculateSuccessRateUseCase()
    return HabitsEffectHandler(
      memosRepository = repository,
      onRefresh = onRefresh,
      buildActivityDataUseCase = buildActivityDataUseCase,
      calculateSuccessRateUseCase = calculateSuccessRateUseCase,
    )
  }
}
