package space.be1ski.vibits.shared.feature.sync.data

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import space.be1ski.vibits.shared.feature.memos.data.mapper.MemoMapper
import space.be1ski.vibits.shared.feature.memos.data.platform.MemoCache
import space.be1ski.vibits.shared.feature.memos.data.remote.MemosApi
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo
import space.be1ski.vibits.shared.feature.sync.domain.model.SyncOperation
import space.be1ski.vibits.shared.feature.sync.domain.model.SyncOperationStatus
import space.be1ski.vibits.shared.feature.sync.domain.model.SyncOperationType
import space.be1ski.vibits.shared.feature.sync.domain.model.SyncStatus
import space.be1ski.vibits.shared.feature.sync.domain.repository.SyncQueueRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock

class SyncOperationApplierTest {
  private val memoMapper = MemoMapper()
  private val baseUrl = "https://memos.example.com"
  private val token = "test-token"

  private fun createApplier(
    handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
  ): Triple<SyncOperationApplier, FakeSyncQueue, RequestTracker> {
    val tracker = RequestTracker()
    val engine =
      MockEngine { request ->
        tracker.record(request)
        handler(request)
      }
    val client =
      HttpClient(engine) {
        install(ContentNegotiation) {
          json(
            Json {
              ignoreUnknownKeys = true
              isLenient = true
            },
          )
        }
      }

    val fakeQueue = FakeSyncQueue()
    val fakeCache = FakeMemoCache()
    val fakeOfflineRepo = OfflineFirstMemosRepository(fakeCache, fakeQueue)
    val applier = SyncOperationApplier(MemosApi(client), memoMapper, fakeQueue, fakeOfflineRepo)
    return Triple(applier, fakeQueue, tracker)
  }

  private fun successResponse(): suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData =
    { request ->
      when (request.method) {
        HttpMethod.Post ->
          respond(
            content = """{"name":"memos/created-1","content":"created","createTime":"2024-01-01T00:00:00Z"}""",
            headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
          )
        HttpMethod.Patch ->
          respond(
            content = """{"name":"memos/1","content":"updated","updateTime":"2024-01-01T00:00:00Z"}""",
            headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
          )
        HttpMethod.Delete ->
          respond(
            content = "",
            headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
          )
        else -> respondError(HttpStatusCode.NotFound)
      }
    }

  // ========== CREATE Operation Tests ==========

  @Test
  fun `when applyOperations with CREATE then calls createMemo`() =
    runTest {
      val (applier, fakeQueue, tracker) = createApplier(successResponse())
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

      applier.applyOperations(listOf(operation), baseUrl, token)

      assertEquals(1, tracker.postCalls)
    }

  @Test
  fun `when CREATE operation succeeds then status becomes SYNCED`() =
    runTest {
      val (applier, fakeQueue, _) = createApplier(successResponse())
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

      applier.applyOperations(listOf(operation), baseUrl, token)

      assertEquals(SyncOperationStatus.SYNCED, fakeQueue.statusHistory["op-1"]?.last())
    }

  @Test
  fun `when CREATE operation has null content then skips and marks as SYNCED`() =
    runTest {
      val (applier, fakeQueue, tracker) = createApplier(successResponse())
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

      applier.applyOperations(listOf(operation), baseUrl, token)

      assertEquals(0, tracker.postCalls)
      assertEquals(SyncOperationStatus.SYNCED, fakeQueue.statusHistory["op-1"]?.last())
    }

  @Test
  fun `when CREATE operation updates memo name from temp to real`() =
    runTest {
      val (applier, fakeQueue, _) = createApplier(successResponse())
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

      applier.applyOperations(listOf(operation), baseUrl, token)

      assertEquals("memos/created-1", fakeQueue.updatedMemoNames["op-1"])
    }

  // ========== UPDATE Operation Tests ==========

  @Test
  fun `when applyOperations with UPDATE then calls updateMemo`() =
    runTest {
      val (applier, fakeQueue, tracker) = createApplier(successResponse())
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

      applier.applyOperations(listOf(operation), baseUrl, token)

      assertEquals(1, tracker.patchCalls)
    }

  @Test
  fun `when UPDATE operation succeeds then status becomes SYNCED`() =
    runTest {
      val (applier, fakeQueue, _) = createApplier(successResponse())
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

      applier.applyOperations(listOf(operation), baseUrl, token)

      assertEquals(SyncOperationStatus.SYNCED, fakeQueue.statusHistory["op-1"]?.last())
    }

  @Test
  fun `when UPDATE operation has null name then skips and marks as SYNCED`() =
    runTest {
      val (applier, fakeQueue, tracker) = createApplier(successResponse())
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

      applier.applyOperations(listOf(operation), baseUrl, token)

      assertEquals(0, tracker.patchCalls)
      assertEquals(SyncOperationStatus.SYNCED, fakeQueue.statusHistory["op-1"]?.last())
    }

  @Test
  fun `when UPDATE operation has null content then skips and marks as SYNCED`() =
    runTest {
      val (applier, fakeQueue, tracker) = createApplier(successResponse())
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

      applier.applyOperations(listOf(operation), baseUrl, token)

      assertEquals(0, tracker.patchCalls)
      assertEquals(SyncOperationStatus.SYNCED, fakeQueue.statusHistory["op-1"]?.last())
    }

  @Test
  fun `when UPDATE operation has temp name then skips update`() =
    runTest {
      val (applier, fakeQueue, tracker) = createApplier(successResponse())
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

      applier.applyOperations(listOf(operation), baseUrl, token)

      assertEquals(0, tracker.patchCalls)
      assertEquals(SyncOperationStatus.SYNCED, fakeQueue.statusHistory["op-1"]?.last())
    }

  // ========== DELETE Operation Tests ==========

  @Test
  fun `when applyOperations with DELETE then calls deleteMemo`() =
    runTest {
      val (applier, fakeQueue, tracker) = createApplier(successResponse())
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

      applier.applyOperations(listOf(operation), baseUrl, token)

      assertEquals(1, tracker.deleteCalls)
    }

  @Test
  fun `when DELETE operation succeeds then status becomes SYNCED`() =
    runTest {
      val (applier, fakeQueue, _) = createApplier(successResponse())
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

      applier.applyOperations(listOf(operation), baseUrl, token)

      assertEquals(SyncOperationStatus.SYNCED, fakeQueue.statusHistory["op-1"]?.last())
    }

  @Test
  fun `when DELETE operation has null name then skips and marks as SYNCED`() =
    runTest {
      val (applier, fakeQueue, tracker) = createApplier(successResponse())
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

      applier.applyOperations(listOf(operation), baseUrl, token)

      assertEquals(0, tracker.deleteCalls)
      assertEquals(SyncOperationStatus.SYNCED, fakeQueue.statusHistory["op-1"]?.last())
    }

  // ========== Error Handling Tests ==========

  @Test
  fun `when operation fails then status becomes FAILED and exception is rethrown`() =
    runTest {
      val (applier, fakeQueue, _) = createApplier { respondError(HttpStatusCode.InternalServerError) }
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
        applier.applyOperations(listOf(operation), baseUrl, token)
      } catch (e: Exception) {
        exceptionThrown = true
      }

      assertTrue(exceptionThrown)
      assertEquals(SyncOperationStatus.FAILED, fakeQueue.statusHistory["op-1"]?.last())
    }

  @Test
  fun `when applyOperations with multiple operations then processes all in order`() =
    runTest {
      val (applier, fakeQueue, tracker) = createApplier(successResponse())
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

      applier.applyOperations(operations, baseUrl, token)

      assertEquals(1, tracker.postCalls)
      assertEquals(1, tracker.patchCalls)
      assertEquals(1, tracker.deleteCalls)
    }

  @Test
  fun `when operation starts then status becomes IN_PROGRESS first`() =
    runTest {
      val (applier, fakeQueue, _) = createApplier(successResponse())
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

      applier.applyOperations(listOf(operation), baseUrl, token)

      val statusHistory = fakeQueue.statusHistory["op-1"]!!
      assertEquals(SyncOperationStatus.IN_PROGRESS, statusHistory.first())
      assertEquals(SyncOperationStatus.SYNCED, statusHistory.last())
    }

  // ========== Helper Classes ==========

  private class RequestTracker {
    var postCalls = 0
      private set
    var patchCalls = 0
      private set
    var deleteCalls = 0
      private set

    fun record(request: HttpRequestData) {
      when (request.method) {
        HttpMethod.Post -> postCalls++
        HttpMethod.Patch -> patchCalls++
        HttpMethod.Delete -> deleteCalls++
      }
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
