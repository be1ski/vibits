package space.be1ski.vibits.feature.memos.data

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import space.be1ski.vibits.feature.memos.data.remote.MemosApi
import kotlin.test.Test
import kotlin.test.assertTrue

class ConnectionTesterImplTest {
  @Test
  fun `when api succeeds then returns success`() =
    runTest {
      val client = createMockClient { respond("""{"memos":[]}""", headers = jsonHeaders()) }
      val tester = ConnectionTesterImpl(MemosApi(client))

      val result = tester("https://example.com", "token")

      assertTrue(result.isSuccess)
    }

  @Test
  fun `when api fails then returns failure`() =
    runTest {
      val client = createMockClient { respondError(HttpStatusCode.Unauthorized) }
      val tester = ConnectionTesterImpl(MemosApi(client))

      val result = tester("https://example.com", "token")

      assertTrue(result.isFailure)
    }

  private fun createMockClient(
    handler: io.ktor.client.engine.mock.MockRequestHandleScope.() -> io.ktor.client.request.HttpResponseData,
  ): HttpClient =
    HttpClient(MockEngine { handler() }) {
      install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
      }
    }

  private fun jsonHeaders() = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
}
