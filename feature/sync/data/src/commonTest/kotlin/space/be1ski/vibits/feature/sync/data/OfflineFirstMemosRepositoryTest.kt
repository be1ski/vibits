package space.be1ski.vibits.feature.sync.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import space.be1ski.vibits.feature.memos.data.platform.MemoCache
import space.be1ski.vibits.feature.memos.domain.model.Memo
import space.be1ski.vibits.feature.sync.domain.model.SyncOperation
import space.be1ski.vibits.feature.sync.domain.model.SyncOperationStatus
import space.be1ski.vibits.feature.sync.domain.model.SyncOperationType
import space.be1ski.vibits.feature.sync.domain.model.SyncStatus
import space.be1ski.vibits.feature.sync.domain.repository.SyncQueueRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class OfflineFirstMemosRepositoryTest {
  private fun createRepository(): Triple<OfflineFirstMemosRepository, FakeMemoCache, FakeSyncQueueRepository> {
    val memoCache = FakeMemoCache()
    val syncQueue = FakeSyncQueueRepository()
    val repository = OfflineFirstMemosRepository(memoCache, syncQueue)
    return Triple(repository, memoCache, syncQueue)
  }

  @Test
  fun `when createMemoLocally then saves to cache and queues CREATE operation`() =
    runTest {
      val (repository, cache, queue) = createRepository()

      val memo = repository.createMemoLocally("Test content")

      // Memo is saved with temp name
      assertTrue(memo.name.startsWith("local_"))
      assertEquals("Test content", memo.content)
      assertNotNull(memo.createTime)
      assertNotNull(memo.updateTime)

      // Memo is in cache
      assertEquals(1, cache.memos.size)
      assertEquals(memo, cache.memos.first())

      // CREATE operation is queued
      assertEquals(1, queue.operations.size)
      val operation = queue.operations.first()
      assertEquals(SyncOperationType.CREATE, operation.type)
      assertEquals(memo.name, operation.memoName)
      assertEquals("Test content", operation.content)
    }

  @Test
  fun `when updateMemoLocally then saves to cache and queues UPDATE operation`() =
    runTest {
      val (repository, cache, queue) = createRepository()
      val existingMemo =
        Memo(
          name = "memos/1",
          content = "Old content",
          createTime = Instant.fromEpochMilliseconds(1000L),
          updateTime = Instant.fromEpochMilliseconds(2000L),
        )
      cache.memos.add(existingMemo)

      val memo = repository.updateMemoLocally("memos/1", "Updated content")

      // Memo is updated with new content
      assertEquals("memos/1", memo.name)
      assertEquals("Updated content", memo.content)
      // Create time is preserved
      assertEquals(Instant.fromEpochMilliseconds(1000L), memo.createTime)
      // Update time is newer
      assertTrue(memo.updateTime!! > existingMemo.updateTime!!)

      // Memo is in cache
      assertEquals(1, cache.memos.size)
      assertEquals("Updated content", cache.memos.first().content)

      // UPDATE operation is queued
      assertEquals(1, queue.operations.size)
      val operation = queue.operations.first()
      assertEquals(SyncOperationType.UPDATE, operation.type)
      assertEquals("memos/1", operation.memoName)
      assertEquals("Updated content", operation.content)
    }

  @Test
  fun `when deleteMemoLocally with server memo then deletes and queues DELETE operation`() =
    runTest {
      val (repository, cache, queue) = createRepository()
      cache.memos.add(Memo(name = "memos/1", content = "Content"))

      repository.deleteMemoLocally("memos/1")

      // Memo is removed from cache
      assertTrue(cache.memos.isEmpty())
      assertTrue(cache.deletedNames.contains("memos/1"))

      // DELETE operation is queued
      assertEquals(1, queue.operations.size)
      val operation = queue.operations.first()
      assertEquals(SyncOperationType.DELETE, operation.type)
      assertEquals("memos/1", operation.memoName)
    }

  @Test
  fun `when deleteMemoLocally with temp memo then deletes without queuing`() =
    runTest {
      val (repository, cache, queue) = createRepository()
      cache.memos.add(Memo(name = "local_123456_78901", content = "Content"))

      repository.deleteMemoLocally("local_123456_78901")

      // Memo is removed from cache
      assertTrue(cache.memos.isEmpty())

      // No operation is queued for temp memos
      assertTrue(queue.operations.isEmpty())
    }

  @Test
  fun `when deleteMemoLocally with temp memo then removes pending CREATE operation`() =
    runTest {
      val (repository, cache, queue) = createRepository()

      // Create a temp memo (this adds a CREATE operation to the queue)
      val memo = repository.createMemoLocally("Content")
      assertEquals(1, queue.operations.size)
      assertEquals(SyncOperationType.CREATE, queue.operations.first().type)

      // Delete the temp memo
      repository.deleteMemoLocally(memo.name)

      // Memo is removed from cache
      assertTrue(cache.memos.isEmpty())

      // Pending CREATE operation is removed
      assertTrue(queue.operations.isEmpty())
    }

  @Test
  fun `when updateMemoLocally with temp memo then coalesces into pending CREATE`() =
    runTest {
      val (repository, cache, queue) = createRepository()

      // Create a temp memo (this adds a CREATE operation to the queue)
      val memo = repository.createMemoLocally("Original content")
      assertEquals(1, queue.operations.size)
      val createOperation = queue.operations.first()
      assertEquals(SyncOperationType.CREATE, createOperation.type)
      assertEquals("Original content", createOperation.content)

      // Update the temp memo
      repository.updateMemoLocally(memo.name, "Updated content")

      // Cache is updated
      assertEquals(1, cache.memos.size)
      assertEquals("Updated content", cache.memos.first().content)

      // Still only one operation (the CREATE), not a separate UPDATE
      assertEquals(1, queue.operations.size)
      assertEquals(SyncOperationType.CREATE, queue.operations.first().type)
      // Content in CREATE operation is updated
      assertEquals("Updated content", queue.operations.first().content)
    }

  @Test
  fun `when updateMemoLocally with temp memo then finds correct CREATE among multiple operations`() =
    runTest {
      val (repository, cache, queue) = createRepository()

      // Add some unrelated operations to the queue first
      queue.addOperation(
        SyncOperation(
          id = "other-1",
          type = SyncOperationType.UPDATE,
          memoName = "memos/other",
          content = "other",
        ),
      )
      queue.addOperation(
        SyncOperation(
          id = "other-2",
          type = SyncOperationType.CREATE,
          memoName = "local_different_name",
          content = "different",
        ),
      )

      // Create a temp memo
      val memo = repository.createMemoLocally("Original content")
      assertEquals(3, queue.operations.size)

      // Update the temp memo - should find the correct CREATE
      repository.updateMemoLocally(memo.name, "Updated content")

      // Only the correct CREATE should be updated
      val targetCreate = queue.operations.find { it.memoName == memo.name }
      assertEquals("Updated content", targetCreate?.content)

      // Other operations unchanged
      assertEquals("other", queue.operations.find { it.id == "other-1" }?.content)
      assertEquals("different", queue.operations.find { it.id == "other-2" }?.content)
    }

  @Test
  fun `when updateMemoLocally with server memo then queues UPDATE operation`() =
    runTest {
      val (repository, cache, queue) = createRepository()
      cache.memos.add(Memo(name = "memos/123", content = "Original"))

      repository.updateMemoLocally("memos/123", "Updated content")

      // UPDATE operation is queued (not coalesced since it's not a temp memo)
      assertEquals(1, queue.operations.size)
      assertEquals(SyncOperationType.UPDATE, queue.operations.first().type)
      assertEquals("memos/123", queue.operations.first().memoName)
      assertEquals("Updated content", queue.operations.first().content)
    }

  @Test
  fun `when updateMemoLocally with non-existing memo then uses current time for createTime`() =
    runTest {
      val (repository, cache, queue) = createRepository()
      // Don't add memo to cache - simulates updating a memo not in cache

      val memo = repository.updateMemoLocally("memos/new", "Content")

      // Memo is created with current time for both createTime and updateTime
      assertNotNull(memo.createTime)
      assertNotNull(memo.updateTime)
      assertEquals(memo.createTime, memo.updateTime)

      // Memo is in cache
      assertEquals(1, cache.memos.size)

      // UPDATE operation is queued
      assertEquals(1, queue.operations.size)
      assertEquals(SyncOperationType.UPDATE, queue.operations.first().type)
    }

  @Test
  fun `when updateMemoLocally with existing memo among others then finds correct one`() =
    runTest {
      val (repository, cache, queue) = createRepository()
      // Add multiple memos, only one matches
      cache.memos.add(Memo(name = "memos/other1", content = "Other 1"))
      cache.memos.add(Memo(name = "memos/target", content = "Original", createTime = Instant.fromEpochMilliseconds(1000L)))
      cache.memos.add(Memo(name = "memos/other2", content = "Other 2"))

      val memo = repository.updateMemoLocally("memos/target", "Updated")

      // Create time is preserved from the found existing memo
      assertEquals(Instant.fromEpochMilliseconds(1000L), memo.createTime)
      assertEquals("Updated", memo.content)
    }

  @Test
  fun `when updateMemoLocally with temp memo and CREATE no longer pending then falls back to UPDATE`() =
    runTest {
      val (repository, cache, queue) = createRepository()

      // Create a temp memo (this adds a CREATE operation to the queue)
      val memo = repository.createMemoLocally("Original content")
      assertEquals(1, queue.operations.size)

      // Simulate race condition: getPendingOperations returns CREATE, but updateContent fails
      queue.simulateUpdateContentRace = true

      // Update the temp memo while sync is racing
      repository.updateMemoLocally(memo.name, "Updated content")

      // Cache is updated
      assertEquals("Updated content", cache.memos.first().content)

      // CREATE content is NOT updated (race condition simulated)
      assertEquals("Original content", queue.operations.first().content)

      // An UPDATE operation is enqueued as fallback
      assertEquals(2, queue.operations.size)
      val updateOp = queue.operations.last()
      assertEquals(SyncOperationType.UPDATE, updateOp.type)
      assertEquals(memo.name, updateOp.memoName)
      assertEquals("Updated content", updateOp.content)
    }

  @Test
  fun `when updateMemoLocally with temp memo and no CREATE in queue then enqueues UPDATE`() =
    runTest {
      val (repository, cache, queue) = createRepository()

      // Add temp memo directly to cache without CREATE operation
      val tempName = "local_123456_78901"
      cache.memos.add(Memo(name = tempName, content = "Original"))

      repository.updateMemoLocally(tempName, "Updated content")

      // Cache is updated
      assertEquals("Updated content", cache.memos.first().content)

      // UPDATE is enqueued since no CREATE was found
      assertEquals(1, queue.operations.size)
      assertEquals(SyncOperationType.UPDATE, queue.operations.first().type)
      assertEquals(tempName, queue.operations.first().memoName)
    }

  @Test
  fun `when getCachedMemos then returns all memos from cache`() =
    runTest {
      val (repository, cache, _) = createRepository()
      cache.memos.add(Memo(name = "memos/1", content = "One"))
      cache.memos.add(Memo(name = "memos/2", content = "Two"))

      val memos = repository.getCachedMemos()

      assertEquals(2, memos.size)
      assertEquals("memos/1", memos[0].name)
      assertEquals("memos/2", memos[1].name)
    }

  @Test
  fun `when replaceAllMemos then replaces cache contents`() =
    runTest {
      val (repository, cache, _) = createRepository()
      cache.memos.add(Memo(name = "memos/old", content = "Old"))

      val newMemos =
        listOf(
          Memo(name = "memos/1", content = "One"),
          Memo(name = "memos/2", content = "Two"),
        )
      repository.replaceAllMemos(newMemos)

      assertEquals(2, cache.memos.size)
      assertEquals("memos/1", cache.memos[0].name)
      assertEquals("memos/2", cache.memos[1].name)
    }

  @Test
  fun `when replaceAllMemos with local temp memos then preserves them`() =
    runTest {
      val (repository, cache, _) = createRepository()
      // Add a local temp memo that should be preserved
      cache.memos.add(Memo(name = "local_123456_78901", content = "Local temp"))
      cache.memos.add(Memo(name = "memos/old", content = "Old"))

      val serverMemos =
        listOf(
          Memo(name = "memos/1", content = "One"),
          Memo(name = "memos/2", content = "Two"),
        )
      repository.replaceAllMemos(serverMemos)

      // Server memos + preserved local temp
      assertEquals(3, cache.memos.size)
      assertTrue(cache.memos.any { it.name == "memos/1" })
      assertTrue(cache.memos.any { it.name == "memos/2" })
      assertTrue(cache.memos.any { it.name == "local_123456_78901" })
    }

  @Test
  fun `when updateLocalMemo with same name then updates memo`() =
    runTest {
      val (repository, cache, _) = createRepository()
      cache.memos.add(Memo(name = "memos/1", content = "Old"))

      val newMemo = Memo(name = "memos/1", content = "New")
      repository.updateLocalMemo("memos/1", newMemo)

      assertEquals(1, cache.memos.size)
      assertEquals("New", cache.memos.first().content)
    }

  @Test
  fun `when updateLocalMemo with different name then removes old and adds new`() =
    runTest {
      val (repository, cache, _) = createRepository()
      cache.memos.add(Memo(name = "local_temp", content = "Temp"))

      val newMemo = Memo(name = "memos/real", content = "Real content")
      repository.updateLocalMemo("local_temp", newMemo)

      // Old name is deleted
      assertTrue(cache.deletedNames.contains("local_temp"))
      // New memo is added
      assertEquals(1, cache.memos.size)
      assertEquals("memos/real", cache.memos.first().name)
      assertEquals("Real content", cache.memos.first().content)
    }

  // ========== Fake Implementations ==========

  private class FakeMemoCache : MemoCache {
    val memos = mutableListOf<Memo>()
    val deletedNames = mutableListOf<String>()

    override suspend fun readMemos(): List<Memo> = memos.toList()

    override suspend fun upsertMemo(memo: Memo) {
      val index = memos.indexOfFirst { it.name == memo.name }
      if (index >= 0) {
        memos[index] = memo
      } else {
        memos.add(memo)
      }
    }

    override suspend fun replaceMemos(memos: List<Memo>) {
      this.memos.clear()
      this.memos.addAll(memos)
    }

    override suspend fun deleteMemo(name: String) {
      deletedNames.add(name)
      memos.removeAll { it.name == name }
    }

    override suspend fun clear() {
      memos.clear()
    }
  }

  private class FakeSyncQueueRepository : SyncQueueRepository {
    val operations = mutableListOf<SyncOperation>()

    /** When true, updateContent always returns false to simulate race condition */
    var simulateUpdateContentRace = false

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
      if (simulateUpdateContentRace) return false
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
