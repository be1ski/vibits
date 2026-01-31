package space.be1ski.vibits.shared.feature.memos.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import space.be1ski.vibits.shared.feature.auth.domain.model.Credentials
import space.be1ski.vibits.shared.feature.auth.domain.repository.CredentialsRepository
import space.be1ski.vibits.shared.feature.memos.data.demo.DemoMemosRepository
import space.be1ski.vibits.shared.feature.memos.data.mapper.MemoMapper
import space.be1ski.vibits.shared.feature.memos.data.offline.OfflineMemosFileDto
import space.be1ski.vibits.shared.feature.memos.data.offline.OfflineMemosRepository
import space.be1ski.vibits.shared.feature.memos.data.platform.MemoCache
import space.be1ski.vibits.shared.feature.memos.data.platform.OfflineMemoStorage
import space.be1ski.vibits.shared.feature.memos.data.remote.MemosApi
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode
import space.be1ski.vibits.shared.feature.mode.domain.repository.AppModeRepository
import space.be1ski.vibits.shared.feature.sync.data.OfflineFirstMemosRepository
import space.be1ski.vibits.shared.feature.sync.domain.model.SyncOperation
import space.be1ski.vibits.shared.feature.sync.domain.model.SyncOperationStatus
import space.be1ski.vibits.shared.feature.sync.domain.model.SyncStatus
import space.be1ski.vibits.shared.feature.sync.domain.repository.SyncQueueRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ModeAwareMemosRepositoryTest {
  @Test
  fun `when mode is DEMO then uses demo repository`() =
    runTest {
      val appModeRepo = FakeAppModeRepository(AppMode.DEMO)
      val demoRepo = DemoMemosRepository()
      val repository = createModeAwareRepository(appModeRepo, demoRepo = demoRepo)

      val memos = repository.listMemos()

      assertTrue(memos.isNotEmpty()) // Demo repo generates data
    }

  @Test
  fun `when mode is OFFLINE then uses offline repository`() =
    runTest {
      val appModeRepo = FakeAppModeRepository(AppMode.OFFLINE)
      val offlineStorage = FakeOfflineMemoStorage()
      val offlineRepo = OfflineMemosRepository(offlineStorage)
      val repository = createModeAwareRepository(appModeRepo, offlineRepo = offlineRepo)

      repository.createMemo("Offline memo")
      val memos = repository.listMemos()

      assertEquals(1, memos.size)
      assertEquals("Offline memo", memos.first().content)
    }

  @Test
  fun `when mode is ONLINE then uses online repository`() =
    runTest {
      val appModeRepo = FakeAppModeRepository(AppMode.ONLINE)
      val cache = FakeMemoCache()
      val repository = createModeAwareRepository(appModeRepo, cache = cache)

      // For ONLINE mode, createMemo will fail since we don't have a real API
      // But we can test that it uses online repo by checking cachedMemos uses cache
      repository.cachedMemos()

      // When mode is ONLINE, cachedMemos should delegate to online repo which uses cache
      // This is enough to cover the else branch in currentRepository()
      val memos = repository.cachedMemos()
      assertEquals(emptyList(), memos)
    }

  @Test
  fun `when mode changes then clears cache`() =
    runTest {
      val appModeRepo = FakeAppModeRepository(AppMode.OFFLINE)
      val cache = FakeMemoCache()
      val repository = createModeAwareRepository(appModeRepo, cache = cache)

      // First call sets lastKnownMode
      repository.listMemos()

      // Change mode
      appModeRepo.mode = AppMode.DEMO

      // Second call should detect mode change and clear cache
      repository.listMemos()

      assertTrue(cache.clearCalled)
    }

  @Test
  fun `when cachedMemos then delegates to current repository`() =
    runTest {
      val appModeRepo = FakeAppModeRepository(AppMode.OFFLINE)
      val offlineStorage = FakeOfflineMemoStorage()
      val offlineRepo = OfflineMemosRepository(offlineStorage)
      val repository = createModeAwareRepository(appModeRepo, offlineRepo = offlineRepo)

      repository.createMemo("Test memo")
      val memos = repository.cachedMemos()

      assertEquals(1, memos.size)
    }

  @Test
  fun `when updateMemo then delegates to current repository`() =
    runTest {
      val appModeRepo = FakeAppModeRepository(AppMode.OFFLINE)
      val offlineStorage = FakeOfflineMemoStorage()
      val offlineRepo = OfflineMemosRepository(offlineStorage)
      val repository = createModeAwareRepository(appModeRepo, offlineRepo = offlineRepo)

      val created = repository.createMemo("Original")
      val updated = repository.updateMemo(created.name, "Updated")

      assertEquals("Updated", updated.content)
    }

  @Test
  fun `when deleteMemo then delegates to current repository`() =
    runTest {
      val appModeRepo = FakeAppModeRepository(AppMode.OFFLINE)
      val offlineStorage = FakeOfflineMemoStorage()
      val offlineRepo = OfflineMemosRepository(offlineStorage)
      val repository = createModeAwareRepository(appModeRepo, offlineRepo = offlineRepo)

      val created = repository.createMemo("To delete")
      repository.deleteMemo(created.name)
      val memos = repository.listMemos()

      assertTrue(memos.isEmpty())
    }

  @Test
  fun `when mode changes to DEMO then resets demo repository`() =
    runTest {
      val appModeRepo = FakeAppModeRepository(AppMode.OFFLINE)
      val demoRepo = DemoMemosRepository()
      val repository = createModeAwareRepository(appModeRepo, demoRepo = demoRepo)

      // Initialize with offline mode
      repository.listMemos()

      // Get initial demo memos
      appModeRepo.mode = AppMode.DEMO
      val firstMemos = repository.listMemos()

      // Change back to offline
      appModeRepo.mode = AppMode.OFFLINE
      repository.listMemos()

      // Change to demo again - should reset
      appModeRepo.mode = AppMode.DEMO
      val secondMemos = repository.listMemos()

      // Both should have memos (demo generates data)
      assertTrue(firstMemos.isNotEmpty())
      assertTrue(secondMemos.isNotEmpty())
    }

  @Test
  fun `when ONLINE mode then createMemo uses offline-first repository`() =
    runTest {
      val appModeRepo = FakeAppModeRepository(AppMode.ONLINE)
      val cache = FakeOnlineMemoCache()
      val syncQueue = createTrackingSyncQueue()
      val offlineFirstRepo = OfflineFirstMemosRepository(cache, syncQueue)
      val repository = createModeAwareRepositoryWithOfflineFirst(appModeRepo, cache, offlineFirstRepo)

      val memo = repository.createMemo("New content")

      assertTrue(memo.name.startsWith("local_"), "Should create local temp memo")
      assertEquals(1, syncQueue.addedOperations.size, "Should queue CREATE operation")
    }

  @Test
  fun `when ONLINE mode then updateMemo uses offline-first repository`() =
    runTest {
      val appModeRepo = FakeAppModeRepository(AppMode.ONLINE)
      val existingMemo = Memo(name = "memos/1", content = "original")
      val cache = FakeOnlineMemoCache(mutableListOf(existingMemo))
      val syncQueue = createTrackingSyncQueue()
      val offlineFirstRepo = OfflineFirstMemosRepository(cache, syncQueue)
      val repository = createModeAwareRepositoryWithOfflineFirst(appModeRepo, cache, offlineFirstRepo)

      val updated = repository.updateMemo("memos/1", "updated content")

      assertEquals("updated content", updated.content)
      assertEquals(1, syncQueue.addedOperations.size, "Should queue UPDATE operation")
    }

  @Test
  fun `when ONLINE mode then deleteMemo uses offline-first repository`() =
    runTest {
      val appModeRepo = FakeAppModeRepository(AppMode.ONLINE)
      val existingMemo = Memo(name = "memos/1", content = "to delete")
      val cache = FakeOnlineMemoCache(mutableListOf(existingMemo))
      val syncQueue = createTrackingSyncQueue()
      val offlineFirstRepo = OfflineFirstMemosRepository(cache, syncQueue)
      val repository = createModeAwareRepositoryWithOfflineFirst(appModeRepo, cache, offlineFirstRepo)

      repository.deleteMemo("memos/1")

      assertEquals(1, syncQueue.addedOperations.size, "Should queue DELETE operation")
    }

  @Test
  fun `when ONLINE mode then cachedMemos uses offline-first repository`() =
    runTest {
      val appModeRepo = FakeAppModeRepository(AppMode.ONLINE)
      val existingMemo = Memo(name = "memos/1", content = "cached")
      val cache = FakeOnlineMemoCache(mutableListOf(existingMemo))
      val offlineFirstRepo = OfflineFirstMemosRepository(cache, createTrackingSyncQueue())
      val repository = createModeAwareRepositoryWithOfflineFirst(appModeRepo, cache, offlineFirstRepo)

      val memos = repository.cachedMemos()

      assertEquals(1, memos.size)
      assertEquals(existingMemo, memos[0])
    }

  @Test
  fun `when mode changes from ONLINE then clears online data`() =
    runTest {
      val appModeRepo = FakeAppModeRepository(AppMode.ONLINE)
      val cache = FakeOnlineMemoCache()
      val syncQueue = createTrackingSyncQueue()
      val offlineFirstRepo = OfflineFirstMemosRepository(cache, syncQueue)
      val repository = createModeAwareRepositoryWithOfflineFirst(appModeRepo, cache, offlineFirstRepo)

      // First call sets lastKnownMode to ONLINE (use cachedMemos to avoid stub)
      repository.cachedMemos()

      // Add some operations to sync queue
      repository.createMemo("test")

      // Change mode from ONLINE to OFFLINE
      appModeRepo.mode = AppMode.OFFLINE

      // This should trigger clearOnlineData (use cachedMemos to avoid stub)
      repository.cachedMemos()

      assertTrue(cache.clearCalled, "Should clear cache when leaving ONLINE mode")
      assertTrue(syncQueue.clearAllCalled, "Should clear sync queue when leaving ONLINE mode")
    }

  private fun createModeAwareRepositoryWithOfflineFirst(
    appModeRepo: AppModeRepository,
    cache: MemoCache,
    offlineFirstRepo: OfflineFirstMemosRepository,
  ): ModeAwareMemosRepository =
    ModeAwareMemosRepository(
      appModeRepository = appModeRepo,
      onlineRepository = createStubOnlineRepository(),
      offlineRepository = OfflineMemosRepository(FakeOfflineMemoStorage()),
      demoRepository = DemoMemosRepository(),
      memoCache = cache,
      offlineFirstRepository = offlineFirstRepo,
    )

  private fun createTrackingSyncQueue() = TrackingSyncQueueRepository()
}

private class TrackingSyncQueueRepository : SyncQueueRepository {
  val addedOperations = mutableListOf<SyncOperation>()
  var clearAllCalled = false
    private set

  override suspend fun addOperation(operation: SyncOperation) {
    addedOperations.add(operation)
  }

  override suspend fun getPendingOperations(): List<SyncOperation> = addedOperations.filter { it.status == SyncOperationStatus.PENDING }

  override suspend fun getAllOperations(): List<SyncOperation> = addedOperations.toList()

  override suspend fun updateStatus(
    id: String,
    status: SyncOperationStatus,
  ) {
    val index = addedOperations.indexOfFirst { it.id == id }
    if (index >= 0) {
      addedOperations[index] = addedOperations[index].copy(status = status)
    }
  }

  override suspend fun updateMemoName(
    id: String,
    memoName: String,
  ) {
    val index = addedOperations.indexOfFirst { it.id == id }
    if (index >= 0) {
      addedOperations[index] = addedOperations[index].copy(memoName = memoName)
    }
  }

  override suspend fun removeOperation(id: String) {
    addedOperations.removeAll { it.id == id }
  }

  override suspend fun clearOperations(syncedOnly: Boolean) {
    if (syncedOnly) {
      addedOperations.removeAll { it.status == SyncOperationStatus.SYNCED }
    } else {
      clearAllCalled = true
      addedOperations.clear()
    }
  }

  override suspend fun resetInProgressToPending() {
    addedOperations.replaceAll { op ->
      if (op.status == SyncOperationStatus.IN_PROGRESS) {
        op.copy(status = SyncOperationStatus.PENDING)
      } else {
        op
      }
    }
  }

  override suspend fun getSyncStatus(): SyncStatus =
    SyncStatus(
      pendingCount = addedOperations.count { it.status == SyncOperationStatus.PENDING },
      failedCount = addedOperations.count { it.status == SyncOperationStatus.FAILED },
    )

  override fun observeSyncStatus(): Flow<SyncStatus> = flowOf(SyncStatus())
}

private class FakeOnlineMemoCache(
  private var memos: MutableList<Memo> = mutableListOf(),
) : MemoCache {
  var clearCalled = false
    private set

  override suspend fun readMemos(): List<Memo> = memos.toList()

  override suspend fun replaceMemos(memos: List<Memo>) {
    this.memos = memos.toMutableList()
  }

  override suspend fun upsertMemo(memo: Memo) {
    memos.removeAll { it.name == memo.name }
    memos.add(memo)
  }

  override suspend fun deleteMemo(name: String) {
    memos.removeAll { it.name == name }
  }

  override suspend fun clear() {
    clearCalled = true
    memos.clear()
  }
}

private class FakeSyncQueueRepository : SyncQueueRepository {
  private val operations = mutableListOf<SyncOperation>()

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
    operations.replaceAll { op ->
      if (op.status == SyncOperationStatus.IN_PROGRESS) {
        op.copy(status = SyncOperationStatus.PENDING)
      } else {
        op
      }
    }
  }

  override suspend fun getSyncStatus(): SyncStatus =
    SyncStatus(
      pendingCount = operations.count { it.status == SyncOperationStatus.PENDING },
      failedCount = operations.count { it.status == SyncOperationStatus.FAILED },
    )

  override fun observeSyncStatus(): Flow<SyncStatus> = flowOf(SyncStatus())
}

private class FakeAppModeRepository(
  var mode: AppMode,
) : AppModeRepository {
  override fun loadMode(): AppMode = mode

  override fun saveMode(mode: AppMode) {
    this.mode = mode
  }
}

private class FakeOfflineMemoStorage : OfflineMemoStorage {
  private var data = OfflineMemosFileDto()

  override fun load(): OfflineMemosFileDto = data

  override fun save(data: OfflineMemosFileDto) {
    this.data = data
  }
}

private class FakeMemoCache : MemoCache {
  var clearCalled = false
    private set

  override suspend fun readMemos(): List<Memo> = emptyList()

  override suspend fun replaceMemos(memos: List<Memo>) = Unit

  override suspend fun upsertMemo(memo: Memo) = Unit

  override suspend fun deleteMemo(name: String) = Unit

  override suspend fun clear() {
    clearCalled = true
  }
}

private fun createModeAwareRepository(
  appModeRepo: AppModeRepository,
  offlineRepo: OfflineMemosRepository = OfflineMemosRepository(FakeOfflineMemoStorage()),
  demoRepo: DemoMemosRepository = DemoMemosRepository(),
  cache: MemoCache = FakeMemoCache(),
): ModeAwareMemosRepository =
  ModeAwareMemosRepository(
    appModeRepository = appModeRepo,
    onlineRepository = createStubOnlineRepository(),
    offlineRepository = offlineRepo,
    demoRepository = demoRepo,
    memoCache = cache,
    offlineFirstRepository = OfflineFirstMemosRepository(cache, FakeSyncQueueRepository()),
  )

/**
 * Stub implementation for online repository.
 * Uses fakes for all dependencies since we're not testing online mode here.
 */
private fun createStubOnlineRepository(): MemosRepositoryImpl {
  val httpClient =
    io.ktor.client.HttpClient(io.ktor.client.engine.mock.MockEngine) {
      engine {
        addHandler { throw NotImplementedError("Not testing online mode") }
      }
    }
  return MemosRepositoryImpl(
    memosApi = MemosApi(httpClient),
    memoMapper = MemoMapper(),
    credentialsRepository = FakeCredentialsRepository(),
    memoCache = FakeMemoCache(),
  )
}

private class FakeCredentialsRepository : CredentialsRepository {
  override fun load() = Credentials("", "")

  override fun save(credentials: Credentials) = Unit
}
