package space.be1ski.vibits.feature.memos.domain.test

import space.be1ski.vibits.feature.memos.domain.model.Memo
import space.be1ski.vibits.feature.memos.domain.repository.MemosRepository

class FakeMemosRepository : MemosRepository {
  var cachedMemosResult: List<Memo> = emptyList()
  var listMemosResult: Result<List<Memo>> = Result.success(emptyList())
  var updateMemoResult: Result<Memo> = Result.success(Memo())
  var createMemoResult: Result<Memo> = Result.success(Memo())
  var deleteMemoResult: Result<Unit> = Result.success(Unit)
  var lastCreatedContent: String = ""
    private set
  var listMemosCalls: Int = 0
    private set
  var cachedMemosCalls: Int = 0
    private set
  var updateMemoCalls: Int = 0
    private set
  var createMemoCalls: Int = 0
    private set
  var deleteMemoCalls: Int = 0
    private set

  override suspend fun listMemos(): List<Memo> {
    listMemosCalls += 1
    return listMemosResult.getOrThrow()
  }

  override suspend fun cachedMemos(): List<Memo> {
    cachedMemosCalls += 1
    return cachedMemosResult
  }

  override suspend fun updateMemo(
    name: String,
    content: String,
  ): Memo {
    updateMemoCalls += 1
    return updateMemoResult.getOrThrow()
  }

  override suspend fun createMemo(content: String): Memo {
    createMemoCalls += 1
    lastCreatedContent = content
    return createMemoResult.getOrThrow()
  }

  override suspend fun deleteMemo(name: String) {
    deleteMemoCalls += 1
    deleteMemoResult.getOrThrow()
  }
}
