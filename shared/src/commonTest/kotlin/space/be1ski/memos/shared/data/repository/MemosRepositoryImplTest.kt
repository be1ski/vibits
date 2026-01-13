package space.be1ski.memos.shared.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.time.Instant
import kotlinx.serialization.json.Json
import space.be1ski.memos.shared.data.mapper.MemoMapper
import space.be1ski.memos.shared.data.remote.MemosApi
import space.be1ski.memos.shared.domain.model.auth.Credentials
import space.be1ski.memos.shared.domain.model.memo.Memo
import space.be1ski.memos.shared.test.FakeCredentialsRepository
import space.be1ski.memos.shared.test.FakeMemoCache

class MemosRepositoryImplTest {
  @Test
  fun `when listMemos called then paginates and caches results`() {
    val calls = mutableListOf<String?>()
    runRepositoryTest(
      handler = { request ->
        val pageToken = request.url.parameters["pageToken"]
        calls += pageToken
        val body = when (pageToken) {
          null -> """{"memos":[{"name":"memos/1","content":"First","createTime":"2024-01-01T00:00:00Z"}],"nextPageToken":"next"}"""
          "next" -> """{"memos":[{"name":"memos/2","content":"Second","createTime":"2024-01-02T00:00:00Z"}],"nextPageToken":"next"}"""
          else -> """{"memos":[]}"""
        }
        respond(
          content = body,
          headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        )
      }
    ) { client ->
      val credentials = FakeCredentialsRepository(Credentials(baseUrl = "https://example.com", token = "token"))
      val cache = FakeMemoCache()
      val repository = MemosRepositoryImpl(
        memosApi = MemosApi(client),
        memoMapper = MemoMapper(),
        credentialsRepository = credentials,
        memoCache = cache
      )

      val result = repository.listMemos()

      assertEquals(2, result.size)
      assertEquals(listOf(null, "next"), calls)
      assertEquals(1, cache.replaceCalls)
      assertEquals(result, cache.readMemos())
    }
  }

  @Test
  fun `when cachedMemos called then reads from cache`() = runRepositoryTest(
    handler = { respond("") }
  ) { client ->
    val cache = FakeMemoCache(
      memos = listOf(Memo(name = "memos/1", content = "Cached", createTime = Instant.parse("2024-01-01T00:00:00Z")))
    )
    val repository = MemosRepositoryImpl(
      memosApi = MemosApi(client),
      memoMapper = MemoMapper(),
      credentialsRepository = FakeCredentialsRepository(Credentials(baseUrl = "https://example.com", token = "token")),
      memoCache = cache
    )

    val result = repository.cachedMemos()

    assertEquals(1, result.size)
    assertEquals("Cached", result.first().content)
  }

  @Test
  fun `when updateMemo called then upserts cache`() = runRepositoryTest(
    handler = { request ->
      if (request.method == HttpMethod.Patch) {
        respond(
          content = """{"name":"memos/1","content":"Updated","updateTime":"2024-02-01T00:00:00Z"}""",
          headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        )
      } else {
        respond("")
      }
    }
  ) { client ->
    val cache = FakeMemoCache()
    val repository = MemosRepositoryImpl(
      memosApi = MemosApi(client),
      memoMapper = MemoMapper(),
      credentialsRepository = FakeCredentialsRepository(Credentials(baseUrl = "https://example.com", token = "token")),
      memoCache = cache
    )

    val updated = repository.updateMemo("memos/1", "Updated")

    assertEquals("Updated", updated.content)
    assertNotNull(cache.upserted)
    assertEquals("Updated", cache.upserted?.content)
  }

  @Test
  fun `when createMemo called then upserts cache`() = runRepositoryTest(
    handler = { request ->
      if (request.method == HttpMethod.Post) {
        respond(
          content = """{"name":"memos/2","content":"Created","createTime":"2024-03-01T00:00:00Z"}""",
          headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        )
      } else {
        respond("")
      }
    }
  ) { client ->
    val cache = FakeMemoCache()
    val repository = MemosRepositoryImpl(
      memosApi = MemosApi(client),
      memoMapper = MemoMapper(),
      credentialsRepository = FakeCredentialsRepository(Credentials(baseUrl = "https://example.com", token = "token")),
      memoCache = cache
    )

    val created = repository.createMemo("Created")

    assertEquals("Created", created.content)
    assertNotNull(cache.upserted)
    assertEquals("Created", cache.upserted?.content)
  }

  @Test
  fun `when deleteMemo called then deletes cache entry`() = runRepositoryTest(
    handler = { request ->
      if (request.method == HttpMethod.Delete) respond("") else respond("")
    }
  ) { client ->
    val cache = FakeMemoCache()
    val repository = MemosRepositoryImpl(
      memosApi = MemosApi(client),
      memoMapper = MemoMapper(),
      credentialsRepository = FakeCredentialsRepository(Credentials(baseUrl = "https://example.com", token = "token")),
      memoCache = cache
    )

    repository.deleteMemo("memos/3")

    assertEquals("memos/3", cache.deletedName)
  }

  @Test
  fun `when listMemos without credentials then throws`() = runRepositoryTest(
    handler = { respond("") }
  ) { client ->
    val repository = MemosRepositoryImpl(
      memosApi = MemosApi(client),
      memoMapper = MemoMapper(),
      credentialsRepository = FakeCredentialsRepository(Credentials(baseUrl = "", token = "")),
      memoCache = FakeMemoCache()
    )

    assertFailsWith<IllegalStateException> { repository.listMemos() }
  }

  private fun runRepositoryTest(
    handler: suspend MockRequestHandleScope.(HttpRequestData) -> io.ktor.client.request.HttpResponseData,
    block: suspend (HttpClient) -> Unit
  ) = kotlinx.coroutines.test.runTest {
    val engine = MockEngine(handler)
    val client = HttpClient(engine) {
      install(ContentNegotiation) {
        json(
          Json {
            ignoreUnknownKeys = true
            isLenient = true
          }
        )
      }
    }

    block(client)
  }

}
