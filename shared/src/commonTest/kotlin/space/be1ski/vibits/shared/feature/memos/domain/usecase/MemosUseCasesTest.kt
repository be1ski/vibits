package space.be1ski.vibits.shared.feature.memos.domain.usecase

import kotlinx.coroutines.test.runTest
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo
import space.be1ski.vibits.shared.test.FakeMemosRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class CreateMemoUseCaseTest {
  @Test
  fun `when invoke then delegates to repository`() = runTest {
    val expectedMemo = Memo(name = "memos/1", content = "test content")
    val repository = FakeMemosRepository().apply {
      createMemoResult = Result.success(expectedMemo)
    }
    val useCase = CreateMemoUseCase(repository)

    val result = useCase("test content")

    assertEquals(expectedMemo, result)
    assertEquals(1, repository.createMemoCalls)
  }
}

class UpdateMemoUseCaseTest {
  @Test
  fun `when invoke then delegates to repository`() = runTest {
    val expectedMemo = Memo(name = "memos/1", content = "updated")
    val repository = FakeMemosRepository().apply {
      updateMemoResult = Result.success(expectedMemo)
    }
    val useCase = UpdateMemoUseCase(repository)

    val result = useCase("memos/1", "updated")

    assertEquals(expectedMemo, result)
    assertEquals(1, repository.updateMemoCalls)
  }
}

class DeleteMemoUseCaseTest {
  @Test
  fun `when invoke then delegates to repository`() = runTest {
    val repository = FakeMemosRepository()
    val useCase = DeleteMemoUseCase(repository)

    useCase("memos/1")

    assertEquals(1, repository.deleteMemoCalls)
  }
}

class LoadMemosUseCaseTest {
  @Test
  fun `when invoke then returns memos from repository`() = runTest {
    val expectedMemos = listOf(Memo(name = "memos/1"), Memo(name = "memos/2"))
    val repository = FakeMemosRepository().apply {
      listMemosResult = Result.success(expectedMemos)
    }
    val useCase = LoadMemosUseCase(repository)

    val result = useCase()

    assertEquals(expectedMemos, result)
    assertEquals(1, repository.listMemosCalls)
  }
}

class LoadCachedMemosUseCaseTest {
  @Test
  fun `when invoke then returns cached memos from repository`() = runTest {
    val expectedMemos = listOf(Memo(name = "memos/1"))
    val repository = FakeMemosRepository().apply {
      cachedMemosResult = expectedMemos
    }
    val useCase = LoadCachedMemosUseCase(repository)

    val result = useCase()

    assertEquals(expectedMemos, result)
    assertEquals(1, repository.cachedMemosCalls)
  }
}
