package space.be1ski.vibits.shared.data.remote

import space.be1ski.vibits.shared.feature.memos.data.remote.MemosApi
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json
import kotlinx.coroutines.test.runTest

class MemosApiTest {
  @Test
  fun `when listMemos then sends auth and pagination`() = runTest {
    val client = clientWithHandler { request ->
      assertEquals(HttpMethod.Get, request.method)
      assertEquals("Bearer token", request.headers[HttpHeaders.Authorization])
      assertEquals("50", request.url.parameters["pageSize"])
      assertEquals("50", request.url.parameters["limit"])
      assertEquals("next", request.url.parameters["pageToken"])
      assertTrue(request.url.toString().startsWith("https://example.com/api/v1/memos"))
      respond(
        content = """{"memos":[]}""",
        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
      )
    }

    val api = MemosApi(client)
    api.listMemos(
      baseUrl = " https://example.com/ ",
      token = "token",
      pageSize = 50,
      pageToken = "next"
    )
  }

  @Test
  fun `when listMemos without pageToken then omits it`() = runTest {
    val client = clientWithHandler { request ->
      assertEquals(null, request.url.parameters["pageToken"])
      respond(
        content = """{"memos":[]}""",
        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
      )
    }

    val api = MemosApi(client)
    api.listMemos(
      baseUrl = "https://example.com",
      token = "token",
      pageSize = 20,
      pageToken = null
    )
  }

  @Test
  fun `when updateMemo then sends patch with body`() = runTest {
    val client = clientWithHandler { request ->
      assertEquals(HttpMethod.Patch, request.method)
      assertEquals("Bearer token", request.headers[HttpHeaders.Authorization])
      assertEquals("content", request.url.parameters["updateMask"])
      assertTrue(bodyText(request).contains("\"content\":\"Updated\""))
      respond(
        content = """{"name":"memos/1","content":"Updated","updateTime":"2024-02-01T00:00:00Z"}""",
        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
      )
    }

    val api = MemosApi(client)
    api.updateMemo(
      baseUrl = "https://example.com/",
      token = "token",
      name = "memos/1",
      content = "Updated"
    )
  }

  @Test
  fun `when createMemo then sends post with body`() = runTest {
    val client = clientWithHandler { request ->
      assertEquals(HttpMethod.Post, request.method)
      assertEquals("Bearer token", request.headers[HttpHeaders.Authorization])
      assertTrue(bodyText(request).contains("\"content\":\"Created\""))
      respond(
        content = """{"name":"memos/2","content":"Created","createTime":"2024-03-01T00:00:00Z"}""",
        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
      )
    }

    val api = MemosApi(client)
    api.createMemo(
      baseUrl = "https://example.com",
      token = "token",
      content = "Created"
    )
  }

  @Test
  fun `when deleteMemo then sends delete`() = runTest {
    val client = clientWithHandler { request ->
      assertEquals(HttpMethod.Delete, request.method)
      assertEquals("Bearer token", request.headers[HttpHeaders.Authorization])
      respond("")
    }

    val api = MemosApi(client)
    api.deleteMemo(
      baseUrl = "https://example.com",
      token = "token",
      name = "memos/3"
    )
  }

  private fun clientWithHandler(
    handler: suspend MockRequestHandleScope.(HttpRequestData) -> io.ktor.client.request.HttpResponseData
  ): HttpClient = HttpClient(MockEngine { request -> handler(request) }) {
    install(ContentNegotiation) {
      json(
        Json {
          ignoreUnknownKeys = true
          isLenient = true
        }
      )
    }
  }

  private fun bodyText(request: HttpRequestData): String {
    val body = request.body
    return when (body) {
      is TextContent -> body.text
      is io.ktor.http.content.ByteArrayContent -> body.bytes().decodeToString()
      else -> body.toString()
    }
  }
}
