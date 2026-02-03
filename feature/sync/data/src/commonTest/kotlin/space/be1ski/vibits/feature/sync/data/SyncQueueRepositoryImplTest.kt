package space.be1ski.vibits.feature.sync.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import space.be1ski.vibits.feature.sync.data.platform.SyncOperationStore
import space.be1ski.vibits.feature.sync.domain.model.SyncOperation
import space.be1ski.vibits.feature.sync.domain.model.SyncOperationStatus
import space.be1ski.vibits.feature.sync.domain.model.SyncOperationType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SyncQueueRepositoryImplTest {
  private fun createRepository(): Pair<SyncQueueRepositoryImpl, FakeSyncOperationStore> {
    val store = FakeSyncOperationStore()
    val repository = SyncQueueRepositoryImpl(store)
    return repository to store
  }

  @Test
  fun `when addOperation then stores operation`() =
    runTest {
      val (repository, store) = createRepository()
      val operation =
        SyncOperation(
          id = "op1",
          type = SyncOperationType.CREATE,
          memoName = "memos/1",
          content = "content",
        )

      repository.addOperation(operation)

      assertEquals(1, store.operations.size)
      assertEquals(operation, store.operations["op1"])
    }

  @Test
  fun `when getPendingOperations then returns only pending`() =
    runTest {
      val (repository, store) = createRepository()
      val pending =
        SyncOperation(
          id = "op1",
          type = SyncOperationType.CREATE,
          status = SyncOperationStatus.PENDING,
        )
      val synced =
        SyncOperation(
          id = "op2",
          type = SyncOperationType.UPDATE,
          status = SyncOperationStatus.SYNCED,
        )
      store.operations["op1"] = pending
      store.operations["op2"] = synced

      val result = repository.getPendingOperations()

      assertEquals(1, result.size)
      assertEquals("op1", result.first().id)
    }

  @Test
  fun `when getAllOperations then returns all`() =
    runTest {
      val (repository, store) = createRepository()
      store.operations["op1"] = SyncOperation(id = "op1", type = SyncOperationType.CREATE)
      store.operations["op2"] = SyncOperation(id = "op2", type = SyncOperationType.UPDATE)

      val result = repository.getAllOperations()

      assertEquals(2, result.size)
    }

  @Test
  fun `when updateStatus then updates operation status`() =
    runTest {
      val (repository, store) = createRepository()
      store.operations["op1"] =
        SyncOperation(
          id = "op1",
          type = SyncOperationType.CREATE,
          status = SyncOperationStatus.PENDING,
        )

      repository.updateStatus("op1", SyncOperationStatus.SYNCED)

      assertEquals(SyncOperationStatus.SYNCED, store.operations["op1"]?.status)
    }

  @Test
  fun `when updateMemoName then updates memo name`() =
    runTest {
      val (repository, store) = createRepository()
      store.operations["op1"] =
        SyncOperation(
          id = "op1",
          type = SyncOperationType.CREATE,
          memoName = "local_123",
        )

      repository.updateMemoName("op1", "memos/456")

      assertEquals("memos/456", store.operations["op1"]?.memoName)
    }

  @Test
  fun `when updateContent with pending operation then updates and returns true`() =
    runTest {
      val (repository, store) = createRepository()
      store.operations["op1"] =
        SyncOperation(
          id = "op1",
          type = SyncOperationType.CREATE,
          content = "old content",
          status = SyncOperationStatus.PENDING,
        )

      val result = repository.updateContent("op1", "new content")

      assertTrue(result)
      assertEquals("new content", store.operations["op1"]?.content)
    }

  @Test
  fun `when updateContent with non-pending operation then returns false`() =
    runTest {
      val (repository, store) = createRepository()
      store.operations["op1"] =
        SyncOperation(
          id = "op1",
          type = SyncOperationType.CREATE,
          content = "old content",
          status = SyncOperationStatus.IN_PROGRESS,
        )

      val result = repository.updateContent("op1", "new content")

      assertTrue(!result)
      assertEquals("old content", store.operations["op1"]?.content)
    }

  @Test
  fun `when removeOperation then removes from store`() =
    runTest {
      val (repository, store) = createRepository()
      store.operations["op1"] = SyncOperation(id = "op1", type = SyncOperationType.CREATE)

      repository.removeOperation("op1")

      assertTrue(store.operations.isEmpty())
    }

  @Test
  fun `when clearOperations with syncedOnly true then removes synced operations`() =
    runTest {
      val (repository, store) = createRepository()
      store.operations["op1"] =
        SyncOperation(
          id = "op1",
          type = SyncOperationType.CREATE,
          status = SyncOperationStatus.SYNCED,
        )
      store.operations["op2"] =
        SyncOperation(
          id = "op2",
          type = SyncOperationType.UPDATE,
          status = SyncOperationStatus.PENDING,
        )

      repository.clearOperations(syncedOnly = true)

      assertEquals(1, store.operations.size)
      assertEquals("op2", store.operations.keys.first())
    }

  @Test
  fun `when getSyncStatus then returns counts`() =
    runTest {
      val (repository, store) = createRepository()
      store.operations["op1"] =
        SyncOperation(
          id = "op1",
          type = SyncOperationType.CREATE,
          status = SyncOperationStatus.PENDING,
        )
      store.operations["op2"] =
        SyncOperation(
          id = "op2",
          type = SyncOperationType.UPDATE,
          status = SyncOperationStatus.PENDING,
        )
      store.operations["op3"] =
        SyncOperation(
          id = "op3",
          type = SyncOperationType.DELETE,
          status = SyncOperationStatus.FAILED,
        )

      val status = repository.getSyncStatus()

      assertEquals(2, status.pendingCount)
      assertEquals(1, status.failedCount)
    }

  @Test
  fun `when observeSyncStatus then emits status updates`() =
    runTest {
      val (repository, store) = createRepository()
      store.operations["op1"] =
        SyncOperation(
          id = "op1",
          type = SyncOperationType.CREATE,
          status = SyncOperationStatus.PENDING,
        )
      store.notifyChange()

      val status = repository.observeSyncStatus().first()

      assertEquals(1, status.pendingCount)
      assertEquals(0, status.failedCount)
    }

  // ========== Fake Implementation ==========

  private class FakeSyncOperationStore : SyncOperationStore {
    val operations = mutableMapOf<String, SyncOperation>()
    private val changeFlow = MutableStateFlow(0)

    fun notifyChange() {
      changeFlow.value++
    }

    override suspend fun upsertOperation(operation: SyncOperation) {
      operations[operation.id] = operation
      notifyChange()
    }

    override suspend fun getPendingOperations(): List<SyncOperation> = operations.values.filter { it.status == SyncOperationStatus.PENDING }

    override suspend fun getAllOperations(): List<SyncOperation> = operations.values.toList()

    override suspend fun updateStatus(
      id: String,
      status: SyncOperationStatus,
    ) {
      operations[id]?.let { operations[id] = it.copy(status = status) }
      notifyChange()
    }

    override suspend fun updateMemoName(
      id: String,
      memoName: String,
    ) {
      operations[id]?.let { operations[id] = it.copy(memoName = memoName) }
      notifyChange()
    }

    override suspend fun updateContent(
      id: String,
      content: String,
    ): Boolean {
      val op = operations[id]
      if (op != null && op.status == SyncOperationStatus.PENDING) {
        operations[id] = op.copy(content = content)
        notifyChange()
        return true
      }
      return false
    }

    override suspend fun removeOperation(id: String) {
      operations.remove(id)
      notifyChange()
    }

    override suspend fun clearOperations(syncedOnly: Boolean) {
      if (syncedOnly) {
        val synced = operations.filter { it.value.status == SyncOperationStatus.SYNCED }.keys
        synced.forEach { operations.remove(it) }
      } else {
        operations.clear()
      }
      notifyChange()
    }

    override suspend fun resetInProgressToPending() {
      operations.forEach { (id, op) ->
        if (op.status == SyncOperationStatus.IN_PROGRESS) {
          operations[id] = op.copy(status = SyncOperationStatus.PENDING)
        }
      }
      notifyChange()
    }

    override fun observePendingCount(): Flow<Int> = changeFlow.map { operations.count { it.value.status == SyncOperationStatus.PENDING } }

    override fun observeFailedCount(): Flow<Int> = changeFlow.map { operations.count { it.value.status == SyncOperationStatus.FAILED } }

    override suspend fun getPendingCount(): Int = operations.count { it.value.status == SyncOperationStatus.PENDING }

    override suspend fun getFailedCount(): Int = operations.count { it.value.status == SyncOperationStatus.FAILED }
  }
}
