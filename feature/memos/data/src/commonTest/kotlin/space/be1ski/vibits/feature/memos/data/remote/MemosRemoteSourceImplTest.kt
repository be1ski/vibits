package space.be1ski.vibits.feature.memos.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import space.be1ski.vibits.feature.auth.domain.model.Credentials
import space.be1ski.vibits.feature.auth.domain.test.FakeCredentialsRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MemosRemoteSourceImplTest {
  @Test
  fun `when listMemos then returns mapped domain memos`() =
    runTest {
      val source = createSource("""{"memos":[{"name":"memos/1","content":"Hello"}]}""")

      val page = source.listMemos(50, null)

      assertEquals(1, page.memos.size)
      assertEquals("memos/1", page.memos[0].name)
      assertEquals("Hello", page.memos[0].content)
    }

  @Test
  fun `when listMemos with nextPageToken then returns token`() =
    runTest {
      val source = createSource("""{"memos":[],"nextPageToken":"page2"}""")

      val page = source.listMemos(50, null)

      assertEquals("page2", page.nextPageToken)
    }

  @Test
  fun `when listMemos with blank nextPageToken then returns null`() =
    runTest {
      val source = createSource("""{"memos":[],"nextPageToken":""}""")

      val page = source.listMemos(50, null)

      assertNull(page.nextPageToken)
    }

  @Test
  fun `when createMemo then returns mapped domain memo`() =
    runTest {
      val source = createSource("""{"name":"memos/2","content":"Created"}""")

      val memo = source.createMemo("Created")

      assertEquals("memos/2", memo.name)
      assertEquals("Created", memo.content)
    }

  @Test
  fun `when updateMemo then returns mapped domain memo`() =
    runTest {
      val source = createSource("""{"name":"memos/1","content":"Updated"}""")

      val memo = source.updateMemo("memos/1", "Updated")

      assertEquals("memos/1", memo.name)
      assertEquals("Updated", memo.content)
    }

  @Test
  fun `when deleteMemo then completes without error`() =
    runTest {
      val source = createSource("")

      source.deleteMemo("memos/1")
    }

  @Test
  fun `when api fails then propagates exception`() =
    runTest {
      val client =
        HttpClient(MockEngine { throw RuntimeException("Network error") }) {
          install(ContentNegotiation) { json() }
        }
      val source =
        MemosRemoteSourceImpl(
          memosApi = MemosApi(client),
          credentialsRepository = FakeCredentialsRepository(Credentials("https://example.com", "token")),
        )

      val error =
        kotlin.test.assertFailsWith<RuntimeException> {
          source.listMemos(50, null)
        }

      assertEquals("Network error", error.message)
    }

  @Test
  fun `when credentials are empty then throws`() =
    runTest {
      val source = createSource("""{"memos":[]}""", Credentials("", ""))

      val error =
        kotlin.test.assertFailsWith<IllegalStateException> {
          source.listMemos(50, null)
        }

      assertTrue(error.message!!.contains("required"))
    }

  private fun createSource(
    responseBody: String,
    credentials: Credentials = Credentials("https://example.com", "token"),
  ): MemosRemoteSourceImpl {
    val client =
      HttpClient(
        MockEngine {
          respond(
            content = responseBody,
            headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
          )
        },
      ) {
        install(ContentNegotiation) {
          json(Json { ignoreUnknownKeys = true })
        }
      }
    return MemosRemoteSourceImpl(
      memosApi = MemosApi(client),
      credentialsRepository = FakeCredentialsRepository(credentials),
    )
  }
}
