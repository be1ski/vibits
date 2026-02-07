package space.be1ski.vibits.feature.sync.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import space.be1ski.vibits.feature.auth.domain.model.Credentials
import space.be1ski.vibits.feature.auth.domain.test.FakeCredentialsRepository
import space.be1ski.vibits.feature.memos.domain.model.Memo
import space.be1ski.vibits.feature.memos.domain.repository.MemoCache
import space.be1ski.vibits.feature.memos.domain.repository.MemosPage
import space.be1ski.vibits.feature.memos.domain.repository.MemosRemoteSource
import space.be1ski.vibits.feature.sync.domain.model.SyncOperation
import space.be1ski.vibits.feature.sync.domain.model.SyncOperationStatus
import space.be1ski.vibits.feature.sync.domain.model.SyncOperationType
import space.be1ski.vibits.feature.sync.domain.model.SyncResult
import space.be1ski.vibits.feature.sync.domain.model.SyncStatus
import space.be1ski.vibits.feature.sync.domain.repository.SyncQueueRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Clock

class SyncEngineImplTest {
  private fun createEngine(
    credentials: Credentials = Credentials(baseUrl = "https://memos.example.com", token = "test-token"),
    remoteSource: FakeMemosRemoteSource = FakeMemosRemoteSource(),
    initialCachedMemos: List<Memo> = emptyList(),
    pendingOperations: List<SyncOperation> = emptyList(),
  ): Triple<SyncEngineImpl, FakeMemoCache, FakeSyncQueueRepository> {
    val credentialsRepository = FakeCredentialsRepository(credentials)
    val syncQueue = FakeSyncQueueRepository()
    pendingOperations.forEach { syncQueue.operations.add(it) }

    val memoCache = FakeMemoCache(initialCachedMemos.toMutableList())
    val offlineFirstRepository = OfflineFirstMemosRepository(memoCache, syncQueue)

    val syncEngine =
      SyncEngineImpl(
        memosRemoteSource = remoteSource,
        credentialsRepository = credentialsRepository,
        syncQueue = syncQueue,
        offlineFirstRepository = offlineFirstRepository,
      )

    return Triple(syncEngine, memoCache, syncQueue)
  }

  // ========== performSync Tests ==========

  @Test
  fun `when performSync with no credentials then returns NoCredentials`() =
    runTest {
      val (engine, _, _) =
        createEngine(
          credentials = Credentials(baseUrl = "", token = ""),
        )

      val result = engine.performSync()

      assertIs<SyncResult.NoCredentials>(result)
    }

  @Test
  fun `when performSync with blank baseUrl then returns NoCredentials`() =
    runTest {
      val (engine, _, _) =
        createEngine(
          credentials = Credentials(baseUrl = "   ", token = "valid-token"),
        )

      val result = engine.performSync()

      assertIs<SyncResult.NoCredentials>(result)
    }

  @Test
  fun `when performSync with blank token then returns NoCredentials`() =
    runTest {
      val (engine, _, _) =
        createEngine(
          credentials = Credentials(baseUrl = "https://example.com", token = "   "),
        )

      val result = engine.performSync()

      assertIs<SyncResult.NoCredentials>(result)
    }

  @Test
  fun `when performSync with no pending operations then replaces local memos with server memos`() =
    runTest {
      val serverMemo = Memo(name = "memos/1", content = "server content")
      val remoteSource = FakeMemosRemoteSource(memos = listOf(serverMemo))
      val (engine, cache, _) =
        createEngine(
          remoteSource = remoteSource,
          pendingOperations = emptyList(),
        )

      val result = engine.performSync()

      assertIs<SyncResult.Success>(result)
      assertEquals(1, result.syncedMemos.size)
      assertEquals("memos/1", result.syncedMemos.first().name)
      assertEquals(1, cache.replaceCalls)
    }

  @Test
  fun `when performSync with pending CREATE operation then creates memo on server`() =
    runTest {
      val remoteSource = FakeMemosRemoteSource()
      val (engine, _, _) =
        createEngine(
          remoteSource = remoteSource,
          initialCachedMemos = listOf(Memo(name = "local_123_456", content = "new content")),
          pendingOperations =
            listOf(
              SyncOperation(
                id = "op-1",
                type = SyncOperationType.CREATE,
                memoName = "local_123_456",
                content = "new content",
                createdAt = Clock.System.now(),
                status = SyncOperationStatus.PENDING,
              ),
            ),
        )

      val result = engine.performSync()

      assertIs<SyncResult.Success>(result)
      assertTrue(remoteSource.createCalls > 0)
    }

  @Test
  fun `when performSync with api error then returns Error`() =
    runTest {
      val remoteSource = FakeMemosRemoteSource(shouldFail = true)
      val (engine, _, _) =
        createEngine(remoteSource = remoteSource)

      val result = engine.performSync()

      assertIs<SyncResult.Error>(result)
    }

  @Test
  fun `when performSync resets IN_PROGRESS operations to PENDING`() =
    runTest {
      val inProgressOp =
        SyncOperation(
          id = "op-1",
          type = SyncOperationType.UPDATE,
          memoName = "memos/1",
          content = "content",
          createdAt = Clock.System.now(),
          status = SyncOperationStatus.IN_PROGRESS,
        )
      val serverMemo = Memo(name = "memos/1", content = "server")
      val remoteSource = FakeMemosRemoteSource(memos = listOf(serverMemo))
      val (engine, _, syncQueue) =
        createEngine(
          remoteSource = remoteSource,
          pendingOperations = listOf(inProgressOp),
        )

      engine.performSync()

      assertTrue(syncQueue.resetInProgressCalls > 0)
    }

  // ========== forceServerSync Tests ==========

  @Test
  fun `when forceServerSync with no credentials then returns NoCredentials`() =
    runTest {
      val (engine, _, _) =
        createEngine(
          credentials = Credentials(baseUrl = "", token = ""),
        )

      val result = engine.forceServerSync()

      assertIs<SyncResult.NoCredentials>(result)
    }

  @Test
  fun `when forceServerSync then discards pending operations and replaces with server data`() =
    runTest {
      val operation =
        SyncOperation(
          id = "op-1",
          type = SyncOperationType.UPDATE,
          memoName = "memos/1",
          content = "local content",
          createdAt = Clock.System.now(),
          status = SyncOperationStatus.PENDING,
        )
      val serverMemo = Memo(name = "memos/1", content = "server content")
      val remoteSource = FakeMemosRemoteSource(memos = listOf(serverMemo))
      val (engine, cache, syncQueue) =
        createEngine(
          remoteSource = remoteSource,
          pendingOperations = listOf(operation),
        )

      val result = engine.forceServerSync()

      assertIs<SyncResult.Success>(result)
      assertTrue(syncQueue.operations.isEmpty())
      assertEquals(1, cache.replaceCalls)
    }

  @Test
  fun `when forceServerSync with api error then returns Error`() =
    runTest {
      val remoteSource = FakeMemosRemoteSource(shouldFail = true)
      val (engine, _, _) =
        createEngine(remoteSource = remoteSource)

      val result = engine.forceServerSync()

      assertIs<SyncResult.Error>(result)
    }

  // ========== forceLocalSync Tests ==========

  @Test
  fun `when forceLocalSync with no credentials then returns NoCredentials`() =
    runTest {
      val (engine, _, _) =
        createEngine(
          credentials = Credentials(baseUrl = "", token = ""),
        )

      val result = engine.forceLocalSync()

      assertIs<SyncResult.NoCredentials>(result)
    }

  @Test
  fun `when forceLocalSync then applies all pending operations`() =
    runTest {
      val remoteSource = FakeMemosRemoteSource()
      val operation =
        SyncOperation(
          id = "op-1",
          type = SyncOperationType.CREATE,
          memoName = "local_123_456",
          content = "local content",
          createdAt = Clock.System.now(),
          status = SyncOperationStatus.PENDING,
        )
      val (engine, _, _) =
        createEngine(
          remoteSource = remoteSource,
          pendingOperations = listOf(operation),
        )

      val result = engine.forceLocalSync()

      assertIs<SyncResult.Success>(result)
      assertTrue(remoteSource.createCalls > 0)
    }

  @Test
  fun `when forceLocalSync with api error then returns Error`() =
    runTest {
      val remoteSource = FakeMemosRemoteSource(shouldFail = true)
      val (engine, _, _) =
        createEngine(remoteSource = remoteSource)

      val result = engine.forceLocalSync()

      assertIs<SyncResult.Error>(result)
    }

  // ========== isSyncing Tests ==========

  @Test
  fun `when not syncing then isSyncing is false`() =
    runTest {
      val (engine, _, _) = createEngine()

      assertFalse(engine.isSyncing)
    }

  // ========== Conflict Detection Tests ==========

  @Test
  fun `when performSync with conflicts then returns Conflict`() =
    runTest {
      val oldTimestamp = kotlin.time.Instant.parse("2024-01-01T00:00:00Z")
      val localMemo = Memo(name = "memos/1", content = "local content")
      val pendingUpdate =
        SyncOperation(
          id = "op-1",
          type = SyncOperationType.UPDATE,
          memoName = "memos/1",
          content = "local content",
          createdAt = oldTimestamp,
          status = SyncOperationStatus.PENDING,
        )
      val serverMemo =
        Memo(
          name = "memos/1",
          content = "server",
          updateTime = kotlin.time.Instant.parse("2024-01-02T00:00:00Z"),
        )
      val remoteSource = FakeMemosRemoteSource(memos = listOf(serverMemo))
      val (engine, _, _) =
        createEngine(
          remoteSource = remoteSource,
          initialCachedMemos = listOf(localMemo),
          pendingOperations = listOf(pendingUpdate),
        )

      val result = engine.performSync()

      assertIs<SyncResult.Conflict>(result)
      assertTrue(result.conflicts.isNotEmpty())
    }

  @Test
  fun `when performSync with pending DELETE operation then deletes on server`() =
    runTest {
      val remoteSource = FakeMemosRemoteSource()
      val pendingDelete =
        SyncOperation(
          id = "op-1",
          type = SyncOperationType.DELETE,
          memoName = "memos/1",
          content = null,
          createdAt = Clock.System.now(),
          status = SyncOperationStatus.PENDING,
        )
      val (engine, _, _) =
        createEngine(
          remoteSource = remoteSource,
          pendingOperations = listOf(pendingDelete),
        )

      val result = engine.performSync()

      assertIs<SyncResult.Success>(result)
      assertTrue(remoteSource.deleteCalls > 0)
    }

  @Test
  fun `when performSync with pending UPDATE operation then updates on server`() =
    runTest {
      val remoteSource =
        FakeMemosRemoteSource(
          memos = listOf(Memo(name = "memos/1", content = "original")),
        )
      val pendingUpdate =
        SyncOperation(
          id = "op-1",
          type = SyncOperationType.UPDATE,
          memoName = "memos/1",
          content = "updated content",
          createdAt = Clock.System.now(),
          status = SyncOperationStatus.PENDING,
        )
      val localMemo = Memo(name = "memos/1", content = "updated content")
      val (engine, _, _) =
        createEngine(
          remoteSource = remoteSource,
          initialCachedMemos = listOf(localMemo),
          pendingOperations = listOf(pendingUpdate),
        )

      val result = engine.performSync()

      assertIs<SyncResult.Success>(result)
      assertTrue(remoteSource.updateCalls > 0)
    }

  // ========== Pagination Tests ==========

  @Test
  fun `when performSync with pagination then fetches all pages`() =
    runTest {
      val page1 = listOf(Memo(name = "memos/1", content = "page1"))
      val page2 = listOf(Memo(name = "memos/2", content = "page2"))
      val remoteSource = FakeMemosRemoteSource(pages = listOf(page1, page2))
      val (engine, _, _) = createEngine(remoteSource = remoteSource)

      val result = engine.performSync()

      assertIs<SyncResult.Success>(result)
      assertEquals(2, remoteSource.listCalls)
      assertEquals(2, result.syncedMemos.size)
    }

  // ========== Fake Implementations ==========

  private class FakeMemosRemoteSource(
    private val memos: List<Memo> = emptyList(),
    private val pages: List<List<Memo>>? = null,
    private val shouldFail: Boolean = false,
  ) : MemosRemoteSource {
    var createCalls = 0
      private set
    var updateCalls = 0
      private set
    var deleteCalls = 0
      private set
    var listCalls = 0
      private set
    private var pageIndex = 0

    override suspend fun listMemos(
      pageSize: Int,
      pageToken: String?,
    ): MemosPage {
      listCalls++
      if (shouldFail) throw RuntimeException("Remote source error")

      if (pages != null) {
        val currentPage = pages[pageIndex]
        pageIndex++
        val nextToken = if (pageIndex < pages.size) "page-$pageIndex" else null
        return MemosPage(memos = currentPage, nextPageToken = nextToken)
      }

      return MemosPage(memos = memos, nextPageToken = null)
    }

    override suspend fun createMemo(content: String): Memo {
      createCalls++
      if (shouldFail) throw RuntimeException("Remote source error")
      return Memo(name = "memos/new-1", content = content)
    }

    override suspend fun updateMemo(
      name: String,
      content: String,
    ): Memo {
      updateCalls++
      if (shouldFail) throw RuntimeException("Remote source error")
      return Memo(name = name, content = content)
    }

    override suspend fun deleteMemo(name: String) {
      deleteCalls++
      if (shouldFail) throw RuntimeException("Remote source error")
    }
  }

  private class FakeMemoCache(
    private val memos: MutableList<Memo> = mutableListOf(),
  ) : MemoCache {
    var replaceCalls = 0
      private set

    override suspend fun readMemos(): List<Memo> = memos.toList()

    override suspend fun replaceMemos(memos: List<Memo>) {
      replaceCalls++
      this.memos.clear()
      this.memos.addAll(memos)
    }

    override suspend fun upsertMemo(memo: Memo) {
      val index = memos.indexOfFirst { it.name == memo.name }
      if (index >= 0) {
        memos[index] = memo
      } else {
        memos.add(memo)
      }
    }

    override suspend fun deleteMemo(name: String) {
      memos.removeAll { it.name == name }
    }

    override suspend fun clear() {
      memos.clear()
    }
  }

  private class FakeSyncQueueRepository : SyncQueueRepository {
    val operations = mutableListOf<SyncOperation>()
    var resetInProgressCalls = 0
      private set

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
      resetInProgressCalls++
      operations.replaceAll { op ->
        if (op.status == SyncOperationStatus.IN_PROGRESS) {
          op.copy(status = SyncOperationStatus.PENDING)
        } else {
          op
        }
      }
    }

    override fun observeSyncStatus(): Flow<SyncStatus> =
      flowOf(
        SyncStatus(
          pendingCount = operations.count { it.status == SyncOperationStatus.PENDING },
          failedCount = operations.count { it.status == SyncOperationStatus.FAILED },
        ),
      )

    override suspend fun getSyncStatus(): SyncStatus =
      SyncStatus(
        pendingCount = operations.count { it.status == SyncOperationStatus.PENDING },
        failedCount = operations.count { it.status == SyncOperationStatus.FAILED },
      )
  }
}
