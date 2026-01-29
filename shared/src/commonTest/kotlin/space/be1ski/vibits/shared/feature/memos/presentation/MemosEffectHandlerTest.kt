package space.be1ski.vibits.shared.feature.memos.presentation

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import space.be1ski.vibits.shared.feature.auth.domain.model.Credentials
import space.be1ski.vibits.shared.feature.auth.domain.usecase.LoadCredentialsUseCase
import space.be1ski.vibits.shared.feature.auth.domain.usecase.SaveCredentialsUseCase
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo
import space.be1ski.vibits.shared.feature.memos.domain.usecase.CreateMemoUseCase
import space.be1ski.vibits.shared.feature.memos.domain.usecase.DeleteMemoUseCase
import space.be1ski.vibits.shared.feature.memos.domain.usecase.LoadCachedMemosUseCase
import space.be1ski.vibits.shared.feature.memos.domain.usecase.LoadMemosUseCase
import space.be1ski.vibits.shared.feature.memos.domain.usecase.UpdateMemoUseCase
import space.be1ski.vibits.shared.feature.memos.presentation.handler.MemosCredentialsEffectHandler
import space.be1ski.vibits.shared.feature.memos.presentation.handler.MemosEffectHandler
import space.be1ski.vibits.shared.feature.memos.presentation.handler.MemosLoadEffectHandler
import space.be1ski.vibits.shared.feature.memos.presentation.handler.MemosWriteEffectHandler
import space.be1ski.vibits.shared.test.FakeCredentialsRepository
import space.be1ski.vibits.shared.test.FakeMemosRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MemosEffectHandlerTest {
  @Test
  fun `when LoadCredentials effect then emits CredentialsLoaded`() =
    runTest {
      val credentialsRepo =
        FakeCredentialsRepository(
          initial = Credentials(baseUrl = "https://test.com", token = "test-token"),
        )
      val handler = createHandler(credentialsRepository = credentialsRepo)

      val actions = handler(MemosEffect.LoadCredentials).toList()

      assertEquals(
        listOf(MemosAction.CredentialsLoaded(baseUrl = "https://test.com", token = "test-token")),
        actions,
      )
    }

  @Test
  fun `when SaveCredentials effect then saves to repository`() =
    runTest {
      val credentialsRepo = FakeCredentialsRepository()
      val handler = createHandler(credentialsRepository = credentialsRepo)

      handler(
        MemosEffect.SaveCredentials(baseUrl = "https://new.com", token = "new-token"),
      ).toList()

      assertEquals("https://new.com", credentialsRepo.stored.baseUrl)
      assertEquals("new-token", credentialsRepo.stored.token)
    }

  @Test
  fun `when LoadCachedMemos effect succeeds then emits CachedMemosLoaded`() =
    runTest {
      val expectedMemos = listOf(Memo(name = "memos/1", content = "cached"))
      val memosRepo =
        FakeMemosRepository().apply {
          cachedMemosResult = expectedMemos
        }
      val handler = createHandler(memosRepository = memosRepo)

      val actions = handler(MemosEffect.LoadCachedMemos).toList()

      assertEquals(listOf(MemosAction.CachedMemosLoaded(expectedMemos)), actions)
    }

  @Test
  fun `when LoadRemoteMemos effect succeeds then emits MemosLoaded`() =
    runTest {
      val expectedMemos = listOf(Memo(name = "memos/1", content = "remote"))
      val memosRepo =
        FakeMemosRepository().apply {
          listMemosResult = Result.success(expectedMemos)
        }
      val handler = createHandler(memosRepository = memosRepo)

      val actions = handler(MemosEffect.LoadRemoteMemos).toList()

      assertEquals(listOf(MemosAction.MemosLoaded(expectedMemos)), actions)
    }

  @Test
  fun `when LoadRemoteMemos effect fails then emits OperationFailed`() =
    runTest {
      val memosRepo =
        FakeMemosRepository().apply {
          listMemosResult = Result.failure(Exception("Network error"))
        }
      val handler = createHandler(memosRepository = memosRepo)

      val actions = handler(MemosEffect.LoadRemoteMemos).toList()

      assertEquals(1, actions.size)
      assertTrue(actions[0] is MemosAction.OperationFailed)
      assertEquals("Network error", (actions[0] as MemosAction.OperationFailed).error)
    }

  @Test
  fun `when CreateMemo effect succeeds then emits MemoCreated`() =
    runTest {
      val expectedMemo = Memo(name = "memos/new", content = "new content")
      val memosRepo =
        FakeMemosRepository().apply {
          createMemoResult = Result.success(expectedMemo)
        }
      val handler = createHandler(memosRepository = memosRepo)

      val actions = handler(MemosEffect.CreateMemo(content = "new content")).toList()

      assertEquals(listOf(MemosAction.MemoCreated(expectedMemo)), actions)
    }

  @Test
  fun `when CreateMemo effect fails then emits OperationFailed`() =
    runTest {
      val memosRepo =
        FakeMemosRepository().apply {
          createMemoResult = Result.failure(Exception("Create failed"))
        }
      val handler = createHandler(memosRepository = memosRepo)

      val actions = handler(MemosEffect.CreateMemo(content = "test")).toList()

      assertEquals(1, actions.size)
      assertTrue(actions[0] is MemosAction.OperationFailed)
    }

  @Test
  fun `when UpdateMemo effect succeeds then emits MemoUpdated`() =
    runTest {
      val expectedMemo = Memo(name = "memos/1", content = "updated")
      val memosRepo =
        FakeMemosRepository().apply {
          updateMemoResult = Result.success(expectedMemo)
        }
      val handler = createHandler(memosRepository = memosRepo)

      val actions =
        handler(
          MemosEffect.UpdateMemo(name = "memos/1", content = "updated"),
        ).toList()

      assertEquals(listOf(MemosAction.MemoUpdated(expectedMemo)), actions)
    }

  @Test
  fun `when UpdateMemo effect fails then emits OperationFailed`() =
    runTest {
      val memosRepo =
        FakeMemosRepository().apply {
          updateMemoResult = Result.failure(Exception("Update failed"))
        }
      val handler = createHandler(memosRepository = memosRepo)

      val actions =
        handler(
          MemosEffect.UpdateMemo(name = "memos/1", content = "updated"),
        ).toList()

      assertTrue(actions[0] is MemosAction.OperationFailed)
    }

  @Test
  fun `when DeleteMemo effect succeeds then emits MemoDeleted`() =
    runTest {
      val memosRepo =
        FakeMemosRepository().apply {
          deleteMemoResult = Result.success(Unit)
        }
      val handler = createHandler(memosRepository = memosRepo)

      val actions = handler(MemosEffect.DeleteMemo(name = "memos/1")).toList()

      assertEquals(listOf(MemosAction.MemoDeleted("memos/1")), actions)
    }

  @Test
  fun `when DeleteMemo effect fails then emits OperationFailed`() =
    runTest {
      val memosRepo =
        FakeMemosRepository().apply {
          deleteMemoResult = Result.failure(Exception("Delete failed"))
        }
      val handler = createHandler(memosRepository = memosRepo)

      val actions = handler(MemosEffect.DeleteMemo(name = "memos/1")).toList()

      assertTrue(actions[0] is MemosAction.OperationFailed)
    }

  private fun createHandler(
    credentialsRepository: FakeCredentialsRepository = FakeCredentialsRepository(),
    memosRepository: FakeMemosRepository = FakeMemosRepository(),
  ): MemosEffectHandler {
    return MemosEffectHandler(
      credentialsHandler =
        MemosCredentialsEffectHandler(
          loadCredentials = LoadCredentialsUseCase(credentialsRepository),
          saveCredentials = SaveCredentialsUseCase(credentialsRepository),
        ),
      loadHandler =
        MemosLoadEffectHandler(
          loadMemos = LoadMemosUseCase(memosRepository),
          loadCachedMemos = LoadCachedMemosUseCase(memosRepository),
        ),
      writeHandler =
        MemosWriteEffectHandler(
          createMemo = CreateMemoUseCase(memosRepository),
          updateMemo = UpdateMemoUseCase(memosRepository),
          deleteMemo = DeleteMemoUseCase(memosRepository),
        ),
    )
  }
}
