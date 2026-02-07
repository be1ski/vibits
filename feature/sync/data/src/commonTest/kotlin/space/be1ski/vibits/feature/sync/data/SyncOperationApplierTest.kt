package space.be1ski.vibits.feature.sync.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import space.be1ski.vibits.feature.memos.domain.model.Memo
import space.be1ski.vibits.feature.memos.domain.repository.MemoCache
import space.be1ski.vibits.feature.memos.domain.repository.MemosPage
import space.be1ski.vibits.feature.memos.domain.repository.MemosRemoteSource
import space.be1ski.vibits.feature.sync.domain.model.SyncOperation
import space.be1ski.vibits.feature.sync.domain.model.SyncOperationStatus
import space.be1ski.vibits.feature.sync.domain.model.SyncOperationType
import space.be1ski.vibits.feature.sync.domain.model.SyncStatus
import space.be1ski.vibits.feature.sync.domain.repository.SyncQueueRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds

class SyncOperationApplierTest {
  private fun createApplier(
    remoteSource: FakeMemosRemoteSource = FakeMemosRemoteSource(),
  ): Triple<SyncOperationApplier, FakeSyncQueue, FakeMemosRemoteSource> = createApplierWithRetry(RetryConfig(maxRetries = 0), remoteSource)

  private fun createApplierWithRetry(
    retryConfig: RetryConfig,
    remoteSource: FakeMemosRemoteSource = FakeMemosRemoteSource(),
  ): Triple<SyncOperationApplier, FakeSyncQueue, FakeMemosRemoteSource> {
    val fakeQueue = FakeSyncQueue()
    val fakeCache = FakeMemoCache()
    val fakeOfflineRepo = OfflineFirstMemosRepository(fakeCache, fakeQueue)
    val applier = SyncOperationApplier(remoteSource, fakeQueue, fakeOfflineRepo, retryConfig)
    return Triple(applier, fakeQueue, remoteSource)
  }

  // ========== CREATE Operation Tests ==========

  @Test
  fun `when applyOperations with CREATE then calls createMemo`() =
    runTest {
      val (applier, fakeQueue, remoteSource) = createApplier()
      val operation =
        SyncOperation(
          id = "op-1",
          type = SyncOperationType.CREATE,
          memoName = "local_123_456",
          content = "new content",
          createdAt = Clock.System.now(),
          status = SyncOperationStatus.PENDING,
        )
      fakeQueue.operations.add(operation)

      applier.applyOperations(listOf(operation))

      assertEquals(1, remoteSource.createCalls)
    }

  @Test
  fun `when CREATE operation succeeds then status becomes SYNCED`() =
    runTest {
      val (applier, fakeQueue, _) = createApplier()
      val operation =
        SyncOperation(
          id = "op-1",
          type = SyncOperationType.CREATE,
          memoName = "local_123_456",
          content = "new content",
          createdAt = Clock.System.now(),
          status = SyncOperationStatus.PENDING,
        )
      fakeQueue.operations.add(operation)

      applier.applyOperations(listOf(operation))

      assertEquals(SyncOperationStatus.SYNCED, fakeQueue.statusHistory["op-1"]?.last())
    }

  @Test
  fun `when CREATE operation has null content then skips and marks as SYNCED`() =
    runTest {
      val (applier, fakeQueue, remoteSource) = createApplier()
      val operation =
        SyncOperation(
          id = "op-1",
          type = SyncOperationType.CREATE,
          memoName = "local_123_456",
          content = null,
          createdAt = Clock.System.now(),
          status = SyncOperationStatus.PENDING,
        )
      fakeQueue.operations.add(operation)

      applier.applyOperations(listOf(operation))

      assertEquals(0, remoteSource.createCalls)
      assertEquals(SyncOperationStatus.SYNCED, fakeQueue.statusHistory["op-1"]?.last())
    }

  @Test
  fun `when CREATE operation updates memo name from temp to real`() =
    runTest {
      val (applier, fakeQueue, _) = createApplier()
      val operation =
        SyncOperation(
          id = "op-1",
          type = SyncOperationType.CREATE,
          memoName = "local_123_456",
          content = "new content",
          createdAt = Clock.System.now(),
          status = SyncOperationStatus.PENDING,
        )
      fakeQueue.operations.add(operation)

      applier.applyOperations(listOf(operation))

      assertEquals("memos/created-1", fakeQueue.updatedMemoNames["op-1"])
    }

  // ========== UPDATE Operation Tests ==========

  @Test
  fun `when applyOperations with UPDATE then calls updateMemo`() =
    runTest {
      val (applier, fakeQueue, remoteSource) = createApplier()
      val operation =
        SyncOperation(
          id = "op-1",
          type = SyncOperationType.UPDATE,
          memoName = "memos/1",
          content = "updated content",
          createdAt = Clock.System.now(),
          status = SyncOperationStatus.PENDING,
        )
      fakeQueue.operations.add(operation)

      applier.applyOperations(listOf(operation))

      assertEquals(1, remoteSource.updateCalls)
    }

  @Test
  fun `when UPDATE operation succeeds then status becomes SYNCED`() =
    runTest {
      val (applier, fakeQueue, _) = createApplier()
      val operation =
        SyncOperation(
          id = "op-1",
          type = SyncOperationType.UPDATE,
          memoName = "memos/1",
          content = "updated content",
          createdAt = Clock.System.now(),
          status = SyncOperationStatus.PENDING,
        )
      fakeQueue.operations.add(operation)

      applier.applyOperations(listOf(operation))

      assertEquals(SyncOperationStatus.SYNCED, fakeQueue.statusHistory["op-1"]?.last())
    }

  @Test
  fun `when UPDATE operation has null name then skips and marks as SYNCED`() =
    runTest {
      val (applier, fakeQueue, remoteSource) = createApplier()
      val operation =
        SyncOperation(
          id = "op-1",
          type = SyncOperationType.UPDATE,
          memoName = null,
          content = "updated content",
          createdAt = Clock.System.now(),
          status = SyncOperationStatus.PENDING,
        )
      fakeQueue.operations.add(operation)

      applier.applyOperations(listOf(operation))

      assertEquals(0, remoteSource.updateCalls)
      assertEquals(SyncOperationStatus.SYNCED, fakeQueue.statusHistory["op-1"]?.last())
    }

  @Test
  fun `when UPDATE operation has null content then skips and marks as SYNCED`() =
    runTest {
      val (applier, fakeQueue, remoteSource) = createApplier()
      val operation =
        SyncOperation(
          id = "op-1",
          type = SyncOperationType.UPDATE,
          memoName = "memos/1",
          content = null,
          createdAt = Clock.System.now(),
          status = SyncOperationStatus.PENDING,
        )
      fakeQueue.operations.add(operation)

      applier.applyOperations(listOf(operation))

      assertEquals(0, remoteSource.updateCalls)
      assertEquals(SyncOperationStatus.SYNCED, fakeQueue.statusHistory["op-1"]?.last())
    }

  @Test
  fun `when UPDATE operation has temp name then skips update`() =
    runTest {
      val (applier, fakeQueue, remoteSource) = createApplier()
      val operation =
        SyncOperation(
          id = "op-1",
          type = SyncOperationType.UPDATE,
          memoName = "local_123456_789",
          content = "updated content",
          createdAt = Clock.System.now(),
          status = SyncOperationStatus.PENDING,
        )
      fakeQueue.operations.add(operation)

      applier.applyOperations(listOf(operation))

      assertEquals(0, remoteSource.updateCalls)
      assertEquals(SyncOperationStatus.SYNCED, fakeQueue.statusHistory["op-1"]?.last())
    }

  // ========== DELETE Operation Tests ==========

  @Test
  fun `when applyOperations with DELETE then calls deleteMemo`() =
    runTest {
      val (applier, fakeQueue, remoteSource) = createApplier()
      val operation =
        SyncOperation(
          id = "op-1",
          type = SyncOperationType.DELETE,
          memoName = "memos/1",
          content = null,
          createdAt = Clock.System.now(),
          status = SyncOperationStatus.PENDING,
        )
      fakeQueue.operations.add(operation)

      applier.applyOperations(listOf(operation))

      assertEquals(1, remoteSource.deleteCalls)
    }

  @Test
  fun `when DELETE operation succeeds then status becomes SYNCED`() =
    runTest {
      val (applier, fakeQueue, _) = createApplier()
      val operation =
        SyncOperation(
          id = "op-1",
          type = SyncOperationType.DELETE,
          memoName = "memos/1",
          content = null,
          createdAt = Clock.System.now(),
          status = SyncOperationStatus.PENDING,
        )
      fakeQueue.operations.add(operation)

      applier.applyOperations(listOf(operation))

      assertEquals(SyncOperationStatus.SYNCED, fakeQueue.statusHistory["op-1"]?.last())
    }

  @Test
  fun `when DELETE operation has null name then skips and marks as SYNCED`() =
    runTest {
      val (applier, fakeQueue, remoteSource) = createApplier()
      val operation =
        SyncOperation(
          id = "op-1",
          type = SyncOperationType.DELETE,
          memoName = null,
          content = null,
          createdAt = Clock.System.now(),
          status = SyncOperationStatus.PENDING,
        )
      fakeQueue.operations.add(operation)

      applier.applyOperations(listOf(operation))

      assertEquals(0, remoteSource.deleteCalls)
      assertEquals(SyncOperationStatus.SYNCED, fakeQueue.statusHistory["op-1"]?.last())
    }

  // ========== Error Handling Tests ==========

  @Test
  fun `when operation fails then status becomes FAILED and exception is rethrown`() =
    runTest {
      val remoteSource = FakeMemosRemoteSource(shouldFail = true)
      val (applier, fakeQueue, _) = createApplier(remoteSource)
      val operation =
        SyncOperation(
          id = "op-1",
          type = SyncOperationType.CREATE,
          memoName = "local_123_456",
          content = "content",
          createdAt = Clock.System.now(),
          status = SyncOperationStatus.PENDING,
        )
      fakeQueue.operations.add(operation)

      var exceptionThrown = false
      try {
        applier.applyOperations(listOf(operation))
      } catch (e: Exception) {
        exceptionThrown = true
      }

      assertTrue(exceptionThrown)
      assertEquals(SyncOperationStatus.FAILED, fakeQueue.statusHistory["op-1"]?.last())
    }

  @Test
  fun `when applyOperations with multiple operations then processes all in order`() =
    runTest {
      val (applier, fakeQueue, remoteSource) = createApplier()
      val operations =
        listOf(
          SyncOperation(
            id = "op-1",
            type = SyncOperationType.CREATE,
            memoName = "local_1",
            content = "content 1",
            createdAt = Clock.System.now(),
            status = SyncOperationStatus.PENDING,
          ),
          SyncOperation(
            id = "op-2",
            type = SyncOperationType.UPDATE,
            memoName = "memos/1",
            content = "content 2",
            createdAt = Clock.System.now(),
            status = SyncOperationStatus.PENDING,
          ),
          SyncOperation(
            id = "op-3",
            type = SyncOperationType.DELETE,
            memoName = "memos/2",
            content = null,
            createdAt = Clock.System.now(),
            status = SyncOperationStatus.PENDING,
          ),
        )
      operations.forEach { fakeQueue.operations.add(it) }

      applier.applyOperations(operations)

      assertEquals(1, remoteSource.createCalls)
      assertEquals(1, remoteSource.updateCalls)
      assertEquals(1, remoteSource.deleteCalls)
    }

  @Test
  fun `when operation starts then status becomes IN_PROGRESS first`() =
    runTest {
      val (applier, fakeQueue, _) = createApplier()
      val operation =
        SyncOperation(
          id = "op-1",
          type = SyncOperationType.CREATE,
          memoName = "local_123_456",
          content = "content",
          createdAt = Clock.System.now(),
          status = SyncOperationStatus.PENDING,
        )
      fakeQueue.operations.add(operation)

      applier.applyOperations(listOf(operation))

      val statusHistory = fakeQueue.statusHistory["op-1"]!!
      assertEquals(SyncOperationStatus.IN_PROGRESS, statusHistory.first())
      assertEquals(SyncOperationStatus.SYNCED, statusHistory.last())
    }

  // ========== Retry Tests ==========

  @Test
  fun `when operation fails and retries succeed then status becomes SYNCED`() =
    runTest {
      val remoteSource = FakeMemosRemoteSource(failUntilCall = 2)
      val retryConfig = RetryConfig(maxRetries = 2, initialDelay = 1.milliseconds, maxDelay = 10.milliseconds)
      val (applier, fakeQueue, _) = createApplierWithRetry(retryConfig, remoteSource)
      val operation =
        SyncOperation(
          id = "op-1",
          type = SyncOperationType.CREATE,
          memoName = "local_123_456",
          content = "content",
          createdAt = Clock.System.now(),
          status = SyncOperationStatus.PENDING,
        )
      fakeQueue.operations.add(operation)

      applier.applyOperations(listOf(operation))

      assertEquals(2, remoteSource.createCalls)
      assertEquals(SyncOperationStatus.SYNCED, fakeQueue.statusHistory["op-1"]?.last())
    }

  @Test
  fun `when operation fails all retries then status becomes FAILED`() =
    runTest {
      val remoteSource = FakeMemosRemoteSource(shouldFail = true)
      val retryConfig = RetryConfig(maxRetries = 2, initialDelay = 1.milliseconds, maxDelay = 10.milliseconds)
      val (applier, fakeQueue, _) = createApplierWithRetry(retryConfig, remoteSource)
      val operation =
        SyncOperation(
          id = "op-1",
          type = SyncOperationType.CREATE,
          memoName = "local_123_456",
          content = "content",
          createdAt = Clock.System.now(),
          status = SyncOperationStatus.PENDING,
        )
      fakeQueue.operations.add(operation)

      var exceptionThrown = false
      try {
        applier.applyOperations(listOf(operation))
      } catch (e: Exception) {
        exceptionThrown = true
      }

      assertTrue(exceptionThrown)
      assertEquals(3, remoteSource.createCalls) // Initial + 2 retries
      assertEquals(SyncOperationStatus.FAILED, fakeQueue.statusHistory["op-1"]?.last())
    }

  @Test
  fun `when operation succeeds on third retry then makes expected number of calls`() =
    runTest {
      val remoteSource = FakeMemosRemoteSource(failUntilCall = 3)
      val retryConfig = RetryConfig(maxRetries = 3, initialDelay = 1.milliseconds, maxDelay = 10.milliseconds)
      val (applier, fakeQueue, _) = createApplierWithRetry(retryConfig, remoteSource)
      val operation =
        SyncOperation(
          id = "op-1",
          type = SyncOperationType.CREATE,
          memoName = "local_123_456",
          content = "content",
          createdAt = Clock.System.now(),
          status = SyncOperationStatus.PENDING,
        )
      fakeQueue.operations.add(operation)

      applier.applyOperations(listOf(operation))

      assertEquals(3, remoteSource.createCalls)
      assertEquals(SyncOperationStatus.SYNCED, fakeQueue.statusHistory["op-1"]?.last())
    }

  @Test
  fun `when retry config has zero retries then fails immediately without retry`() =
    runTest {
      val remoteSource = FakeMemosRemoteSource(shouldFail = true)
      val retryConfig = RetryConfig(maxRetries = 0, initialDelay = 1.milliseconds, maxDelay = 10.milliseconds)
      val (applier, fakeQueue, _) = createApplierWithRetry(retryConfig, remoteSource)
      val operation =
        SyncOperation(
          id = "op-1",
          type = SyncOperationType.CREATE,
          memoName = "local_123_456",
          content = "content",
          createdAt = Clock.System.now(),
          status = SyncOperationStatus.PENDING,
        )
      fakeQueue.operations.add(operation)

      var exceptionThrown = false
      try {
        applier.applyOperations(listOf(operation))
      } catch (e: Exception) {
        exceptionThrown = true
      }

      assertTrue(exceptionThrown)
      assertEquals(1, remoteSource.createCalls)
      assertEquals(SyncOperationStatus.FAILED, fakeQueue.statusHistory["op-1"]?.last())
    }

  // ========== Helper Classes ==========

  private class FakeMemosRemoteSource(
    private val shouldFail: Boolean = false,
    private val failUntilCall: Int = 0,
  ) : MemosRemoteSource {
    var createCalls = 0
      private set
    var updateCalls = 0
      private set
    var deleteCalls = 0
      private set
    private var totalCalls = 0

    private fun checkFail() {
      totalCalls++
      if (shouldFail || totalCalls < failUntilCall) {
        throw RuntimeException("Remote source error")
      }
    }

    override suspend fun listMemos(
      pageSize: Int,
      pageToken: String?,
    ): MemosPage = MemosPage(memos = emptyList(), nextPageToken = null)

    override suspend fun createMemo(content: String): Memo {
      createCalls++
      checkFail()
      return Memo(name = "memos/created-1", content = content)
    }

    override suspend fun updateMemo(
      name: String,
      content: String,
    ): Memo {
      updateCalls++
      checkFail()
      return Memo(name = name, content = content)
    }

    override suspend fun deleteMemo(name: String) {
      deleteCalls++
      checkFail()
    }
  }

  private class FakeSyncQueue : SyncQueueRepository {
    val operations = mutableListOf<SyncOperation>()
    val statusHistory = mutableMapOf<String, MutableList<SyncOperationStatus>>()
    val updatedMemoNames = mutableMapOf<String, String>()

    override suspend fun addOperation(operation: SyncOperation) {
      operations.add(operation)
    }

    override suspend fun getPendingOperations(): List<SyncOperation> = operations.filter { it.status == SyncOperationStatus.PENDING }

    override suspend fun getAllOperations(): List<SyncOperation> = operations.toList()

    override suspend fun updateStatus(
      id: String,
      status: SyncOperationStatus,
    ) {
      statusHistory.getOrPut(id) { mutableListOf() }.add(status)
      val index = operations.indexOfFirst { it.id == id }
      if (index >= 0) {
        operations[index] = operations[index].copy(status = status)
      }
    }

    override suspend fun updateMemoName(
      id: String,
      memoName: String,
    ) {
      updatedMemoNames[id] = memoName
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
      operations.replaceAll { op ->
        if (op.status == SyncOperationStatus.IN_PROGRESS) {
          op.copy(status = SyncOperationStatus.PENDING)
        } else {
          op
        }
      }
    }

    override fun observeSyncStatus(): Flow<SyncStatus> = flowOf(SyncStatus(pendingCount = 0, failedCount = 0))

    override suspend fun getSyncStatus(): SyncStatus = SyncStatus(pendingCount = 0, failedCount = 0)
  }

  private class FakeMemoCache : MemoCache {
    private val memos = mutableListOf<Memo>()

    override suspend fun readMemos(): List<Memo> = memos.toList()

    override suspend fun replaceMemos(memos: List<Memo>) {
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
}
