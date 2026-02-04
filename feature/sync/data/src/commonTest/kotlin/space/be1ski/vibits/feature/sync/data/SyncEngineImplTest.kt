package space.be1ski.vibits.feature.sync.data

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
import space.be1ski.vibits.feature.auth.domain.model.Credentials
import space.be1ski.vibits.feature.main.test.FakeCredentialsRepository
import space.be1ski.vibits.feature.memos.data.platform.MemoCache
import space.be1ski.vibits.feature.memos.data.remote.MemosApi
import space.be1ski.vibits.feature.memos.domain.model.Memo
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
    handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
    initialCachedMemos: List<Memo> = emptyList(),
    pendingOperations: List<SyncOperation> = emptyList(),
  ): Triple<SyncEngineImpl, FakeMemoCache, FakeSyncQueueRepository> {
    val engine = MockEngine(handler)
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

    val credentialsRepository = FakeCredentialsRepository(credentials)
    val syncQueue = FakeSyncQueueRepository()
    pendingOperations.forEach { syncQueue.operations.add(it) }

    val memoCache = FakeMemoCache(initialCachedMemos.toMutableList())
    val offlineFirstRepository = OfflineFirstMemosRepository(memoCache, syncQueue)

    val syncEngine =
      SyncEngineImpl(
        memosApi = MemosApi(client),
        credentialsRepository = credentialsRepository,
        syncQueue = syncQueue,
        offlineFirstRepository = offlineFirstRepository,
      )

    return Triple(syncEngine, memoCache, syncQueue)
  }

  private fun successResponse(memos: String = "[]"): suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData =
    { request ->
      when (request.method) {
        HttpMethod.Get ->
          respond(
            content = """{"memos":$memos,"nextPageToken":null}""",
            headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
          )
        HttpMethod.Post ->
          respond(
            content = """{"name":"memos/new-1","content":"created","createTime":"2024-01-01T00:00:00Z"}""",
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

  // ========== performSync Tests ==========

  @Test
  fun `when performSync with no credentials then returns NoCredentials`() =
    runTest {
      val (engine, _, _) =
        createEngine(
          credentials = Credentials(baseUrl = "", token = ""),
          handler = successResponse(),
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
          handler = successResponse(),
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
          handler = successResponse(),
        )

      val result = engine.performSync()

      assertIs<SyncResult.NoCredentials>(result)
    }

  @Test
  fun `when performSync with no pending operations then replaces local memos with server memos`() =
    runTest {
      val (engine, cache, _) =
        createEngine(
          handler = successResponse("""[{"name":"memos/1","content":"server content"}]"""),
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
      var createCalled = false
      val (engine, _, _) =
        createEngine(
          handler = { request ->
            when (request.method) {
              HttpMethod.Get ->
                respond(
                  content = """{"memos":[],"nextPageToken":null}""",
                  headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                )
              HttpMethod.Post -> {
                createCalled = true
                respond(
                  content = """{"name":"memos/new-1","content":"new content","createTime":"2024-01-01T00:00:00Z"}""",
                  headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                )
              }
              else -> respondError(HttpStatusCode.NotFound)
            }
          },
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
      assertTrue(createCalled)
    }

  @Test
  fun `when performSync with api error then returns Error`() =
    runTest {
      val (engine, _, _) =
        createEngine(
          handler = { respondError(HttpStatusCode.InternalServerError) },
        )

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
      val (engine, _, syncQueue) =
        createEngine(
          handler = successResponse("""[{"name":"memos/1","content":"server"}]"""),
          pendingOperations = listOf(inProgressOp),
        )

      engine.performSync()

      // The operation should have been reset from IN_PROGRESS
      // (the resetInProgressToPending call happens at the start of sync)
      assertTrue(syncQueue.resetInProgressCalls > 0)
    }

  // ========== forceServerSync Tests ==========

  @Test
  fun `when forceServerSync with no credentials then returns NoCredentials`() =
    runTest {
      val (engine, _, _) =
        createEngine(
          credentials = Credentials(baseUrl = "", token = ""),
          handler = successResponse(),
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
      val (engine, cache, syncQueue) =
        createEngine(
          handler = successResponse("""[{"name":"memos/1","content":"server content"}]"""),
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
      val (engine, _, _) =
        createEngine(
          handler = { respondError(HttpStatusCode.InternalServerError) },
        )

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
          handler = successResponse(),
        )

      val result = engine.forceLocalSync()

      assertIs<SyncResult.NoCredentials>(result)
    }

  @Test
  fun `when forceLocalSync then applies all pending operations`() =
    runTest {
      var createCalled = false
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
          handler = { request ->
            when (request.method) {
              HttpMethod.Get ->
                respond(
                  content = """{"memos":[],"nextPageToken":null}""",
                  headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                )
              HttpMethod.Post -> {
                createCalled = true
                respond(
                  content = """{"name":"memos/new-1","content":"local content","createTime":"2024-01-01T00:00:00Z"}""",
                  headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                )
              }
              else -> respondError(HttpStatusCode.NotFound)
            }
          },
          pendingOperations = listOf(operation),
        )

      val result = engine.forceLocalSync()

      assertIs<SyncResult.Success>(result)
      assertTrue(createCalled)
    }

  @Test
  fun `when forceLocalSync with api error then returns Error`() =
    runTest {
      val (engine, _, _) =
        createEngine(
          handler = { respondError(HttpStatusCode.InternalServerError) },
        )

      val result = engine.forceLocalSync()

      assertIs<SyncResult.Error>(result)
    }

  // ========== isSyncing Tests ==========

  @Test
  fun `when not syncing then isSyncing is false`() =
    runTest {
      val (engine, _, _) =
        createEngine(
          handler = successResponse(),
        )

      assertFalse(engine.isSyncing)
    }

  // ========== Conflict Detection Tests ==========

  @Test
  fun `when performSync with conflicts then returns Conflict`() =
    runTest {
      // Create operation with old timestamp so server version is "newer"
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
      val (engine, _, _) =
        createEngine(
          handler = { request ->
            when (request.method) {
              HttpMethod.Get -> {
                // Server has updateTime newer than operation's createdAt
                val memosJson = """[{"name":"memos/1","content":"server","updateTime":"2024-01-02T00:00:00Z"}]"""
                respond(
                  content = """{"memos":$memosJson,"nextPageToken":null}""",
                  headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                )
              }
              else -> respondError(HttpStatusCode.NotFound)
            }
          },
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
      var deleteCalled = false
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
          handler = { request ->
            when (request.method) {
              HttpMethod.Get ->
                respond(
                  content = """{"memos":[],"nextPageToken":null}""",
                  headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                )
              HttpMethod.Delete -> {
                deleteCalled = true
                respond(
                  content = "",
                  headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                )
              }
              else -> respondError(HttpStatusCode.NotFound)
            }
          },
          pendingOperations = listOf(pendingDelete),
        )

      val result = engine.performSync()

      assertIs<SyncResult.Success>(result)
      assertTrue(deleteCalled)
    }

  @Test
  fun `when performSync with pending UPDATE operation then updates on server`() =
    runTest {
      var updateCalled = false
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
          handler = { request ->
            when (request.method) {
              HttpMethod.Get ->
                respond(
                  content = """{"memos":[{"name":"memos/1","content":"original"}],"nextPageToken":null}""",
                  headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                )
              HttpMethod.Patch -> {
                updateCalled = true
                respond(
                  content = """{"name":"memos/1","content":"updated content","updateTime":"2024-01-01T00:00:00Z"}""",
                  headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                )
              }
              else -> respondError(HttpStatusCode.NotFound)
            }
          },
          initialCachedMemos = listOf(localMemo),
          pendingOperations = listOf(pendingUpdate),
        )

      val result = engine.performSync()

      assertIs<SyncResult.Success>(result)
      assertTrue(updateCalled)
    }

  // ========== Pagination Tests ==========

  @Test
  fun `when performSync with pagination then fetches all pages`() =
    runTest {
      var pagesCalled = 0
      val (engine, _, _) =
        createEngine(
          handler = { request ->
            if (request.method == HttpMethod.Get) {
              pagesCalled++
              val pageToken = request.url.parameters["pageToken"]
              val response =
                if (pageToken == null) {
                  """{"memos":[{"name":"memos/1","content":"page1"}],"nextPageToken":"page2"}"""
                } else {
                  """{"memos":[{"name":"memos/2","content":"page2"}],"nextPageToken":null}"""
                }
              respond(
                content = response,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
              )
            } else {
              respondError(HttpStatusCode.NotFound)
            }
          },
        )

      val result = engine.performSync()

      assertIs<SyncResult.Success>(result)
      assertEquals(2, pagesCalled)
      assertEquals(2, result.syncedMemos.size)
    }

  // ========== Fake Implementations ==========

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
