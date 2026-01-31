package space.be1ski.vibits.feature.main.test

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import space.be1ski.vibits.core.platform.env.LocalConfigProvider
import space.be1ski.vibits.core.platform.mode.AppMode
import space.be1ski.vibits.feature.auth.domain.model.Credentials
import space.be1ski.vibits.feature.auth.domain.repository.CredentialsRepository
import space.be1ski.vibits.feature.memos.data.offline.OfflineMemosFileDto
import space.be1ski.vibits.feature.memos.data.platform.MemoCache
import space.be1ski.vibits.feature.memos.data.platform.OfflineMemoStorage
import space.be1ski.vibits.feature.memos.domain.model.Memo
import space.be1ski.vibits.feature.memos.domain.repository.MemoStorageManager
import space.be1ski.vibits.feature.memos.domain.repository.MemosRepository
import space.be1ski.vibits.feature.mode.domain.repository.AppModeRepository
import space.be1ski.vibits.feature.onboarding.domain.repository.OnboardingStore
import space.be1ski.vibits.feature.settings.domain.model.TimeRangeTab
import space.be1ski.vibits.feature.settings.domain.model.UserPreferences
import space.be1ski.vibits.feature.settings.domain.repository.PreferencesRepository
import space.be1ski.vibits.feature.sync.domain.SyncEngine
import space.be1ski.vibits.feature.sync.domain.model.SyncOperation
import space.be1ski.vibits.feature.sync.domain.model.SyncOperationStatus
import space.be1ski.vibits.feature.sync.domain.model.SyncResult
import space.be1ski.vibits.feature.sync.domain.model.SyncStatus
import space.be1ski.vibits.feature.sync.domain.repository.SyncQueueRepository

class FakeOnboardingStore(
  private var completed: Boolean = false,
) : OnboardingStore {
  override fun isOnboardingCompleted(): Boolean = completed

  override fun markOnboardingCompleted() {
    completed = true
  }

  override fun reset() {
    completed = false
  }
}

class FakeMemoStorageManager : MemoStorageManager {
  var clearAllCalls: Int = 0
    private set

  override fun clearAll() {
    clearAllCalls += 1
  }
}

class FakeCredentialsRepository(
  initial: Credentials = Credentials(baseUrl = "", token = ""),
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
  private var memos: List<Memo> = emptyList(),
) : MemoCache {
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

class FakeAppModeRepository(
  initial: AppMode = AppMode.NOT_SELECTED,
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

class FakePreferencesRepository(
  initial: UserPreferences = UserPreferences(TimeRangeTab.WEEKS, TimeRangeTab.WEEKS),
) : PreferencesRepository {
  var stored: UserPreferences = initial
    private set
  var saveCalls: Int = 0
    private set

  override fun load(): UserPreferences = stored

  override fun save(preferences: UserPreferences) {
    stored = preferences
    saveCalls += 1
  }
}

class FakeOfflineMemoStorage(
  initial: OfflineMemosFileDto = OfflineMemosFileDto(memos = emptyList()),
) : OfflineMemoStorage {
  var stored: OfflineMemosFileDto = initial
    private set
  var saveCalls: Int = 0
    private set

  override fun load(): OfflineMemosFileDto = stored

  override fun save(data: OfflineMemosFileDto) {
    stored = data
    saveCalls += 1
  }
}

fun createFakeLocalConfigProvider(config: Map<String, String> = emptyMap()): LocalConfigProvider =
  LocalConfigProvider { key -> config[key] }

class FakeSyncEngine : SyncEngine {
  var performSyncResult: SyncResult = SyncResult.Success(emptyList())
  var forceLocalSyncResult: SyncResult = SyncResult.Success(emptyList())
  var forceServerSyncResult: SyncResult = SyncResult.Success(emptyList())
  override var isSyncing: Boolean = false

  override suspend fun performSync(): SyncResult = performSyncResult

  override suspend fun forceLocalSync(): SyncResult = forceLocalSyncResult

  override suspend fun forceServerSync(): SyncResult = forceServerSyncResult
}

@Suppress("TooManyFunctions") // Repository interface requires many methods
class FakeSyncQueueRepository : SyncQueueRepository {
  val operations = mutableListOf<SyncOperation>()
  var syncStatus = SyncStatus(pendingCount = 0, failedCount = 0)

  override suspend fun addOperation(operation: SyncOperation) {
    operations.add(operation)
  }

  override suspend fun getPendingOperations(): List<SyncOperation> = operations.filter { it.status == SyncOperationStatus.PENDING }

  override suspend fun getAllOperations(): List<SyncOperation> = operations.toList()

  override suspend fun updateStatus(
    id: String,
    status: SyncOperationStatus,
  ) {
    val index = operations.indexOfFirst { it.id == id }
    if (index >= 0) {
      operations[index] = operations[index].copy(status = status)
    }
  }

  override suspend fun updateMemoName(
    id: String,
    memoName: String,
  ) {
    val index = operations.indexOfFirst { it.id == id }
    if (index >= 0) {
      operations[index] = operations[index].copy(memoName = memoName)
    }
  }

  override suspend fun updateContent(
    id: String,
    content: String,
  ): Boolean {
    val index = operations.indexOfFirst { it.id == id && it.status == SyncOperationStatus.PENDING }
    if (index >= 0) {
      operations[index] = operations[index].copy(content = content)
      return true
    }
    return false
  }

  override suspend fun removeOperation(id: String) {
    operations.removeAll { it.id == id }
  }

  override suspend fun clearOperations(syncedOnly: Boolean) {
    if (syncedOnly) {
      operations.removeAll { it.status == SyncOperationStatus.SYNCED }
    } else {
      operations.clear()
    }
  }

  override suspend fun resetInProgressToPending() {
    val updated =
      operations.map { op ->
        if (op.status == SyncOperationStatus.IN_PROGRESS) {
          op.copy(status = SyncOperationStatus.PENDING)
        } else {
          op
        }
      }
    operations.clear()
    operations.addAll(updated)
  }

  override fun observeSyncStatus(): Flow<SyncStatus> = flowOf(syncStatus)

  override suspend fun getSyncStatus(): SyncStatus = syncStatus
}
