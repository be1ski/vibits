package space.be1ski.memos.shared.test

import space.be1ski.memos.shared.feature.memos.data.local.MemoCache
import space.be1ski.memos.shared.feature.auth.domain.model.Credentials
import space.be1ski.memos.shared.feature.memos.domain.model.Memo
import space.be1ski.memos.shared.feature.auth.domain.repository.CredentialsRepository
import space.be1ski.memos.shared.feature.memos.domain.repository.MemosRepository
import space.be1ski.memos.shared.feature.mode.domain.model.AppMode
import space.be1ski.memos.shared.feature.mode.domain.repository.AppModeRepository

class FakeCredentialsRepository(
  initial: Credentials = Credentials(baseUrl = "", token = "")
) : CredentialsRepository {
  var stored: Credentials = initial
    private set
  var saveCount: Int = 0
    private set

  override fun load(): Credentials = stored

  override fun save(credentials: Credentials) {
    stored = credentials
    saveCount += 1
  }
}

class FakeMemoCache(
  private var memos: List<Memo> = emptyList()
) : MemoCache() {
  var replaceCalls: Int = 0
    private set
  var upserted: Memo? = null
    private set
  var deletedName: String? = null
    private set
  var clearCalls: Int = 0
    private set

  override suspend fun readMemos(): List<Memo> = memos

  override suspend fun replaceMemos(memos: List<Memo>) {
    replaceCalls += 1
    this.memos = memos
  }

  override suspend fun upsertMemo(memo: Memo) {
    upserted = memo
    memos = memos.filterNot { it.name == memo.name } + memo
  }

  override suspend fun deleteMemo(name: String) {
    deletedName = name
    memos = memos.filterNot { it.name == name }
  }

  override suspend fun clear() {
    clearCalls += 1
    memos = emptyList()
  }
}

class FakeMemosRepository : MemosRepository {
  var cachedMemosResult: List<Memo> = emptyList()
  var listMemosResult: Result<List<Memo>> = Result.success(emptyList())
  var updateMemoResult: Result<Memo> = Result.success(Memo())
  var createMemoResult: Result<Memo> = Result.success(Memo())
  var deleteMemoResult: Result<Unit> = Result.success(Unit)
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

  override suspend fun updateMemo(name: String, content: String): Memo {
    updateMemoCalls += 1
    return updateMemoResult.getOrThrow()
  }

  override suspend fun createMemo(content: String): Memo {
    createMemoCalls += 1
    return createMemoResult.getOrThrow()
  }

  override suspend fun deleteMemo(name: String) {
    deleteMemoCalls += 1
    deleteMemoResult.getOrThrow()
  }
}

class FakeAppModeRepository(
  initial: AppMode = AppMode.NotSelected
) : AppModeRepository {
  var storedMode: AppMode = initial
    private set
  var saveCalls: Int = 0
    private set

  override fun loadMode(): AppMode = storedMode

  override fun saveMode(mode: AppMode) {
    storedMode = mode
    saveCalls += 1
  }
}
