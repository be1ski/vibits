package space.be1ski.vibits.feature.changelog.data

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val TEST_RELEASES_URL = "https://api.github.com/repos/test/repo/releases"

class GitHubReleasesApiTest {
  @Test
  fun `when getReleases then sends GET with accept header`() =
    runTest {
      val client =
        clientWithHandler { request ->
          assertEquals(HttpMethod.Get, request.method)
          assertEquals(
            "application/vnd.github+json",
            request.headers[HttpHeaders.Accept],
          )
          assertTrue(request.url.toString().startsWith(TEST_RELEASES_URL))
          respond(
            content = "[]",
            headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
          )
        }

      val api = GitHubReleasesApi(client, TEST_RELEASES_URL)
      val result = api.getReleases()

      assertTrue(result.isEmpty())
    }

  @Test
  fun `when getReleases with data then parses DTOs`() =
    runTest {
      val json =
        """
        [
          {
            "tag_name": "v1.2.0",
            "name": "Release 1.2.0",
            "body": "## Changes\n* Added feature X",
            "published_at": "2026-02-15T12:00:00Z"
          },
          {
            "tag_name": "v1.1.0",
            "name": "Release 1.1.0",
            "body": "Bug fixes",
            "published_at": "2026-02-01T12:00:00Z"
          }
        ]
        """.trimIndent()

      val client =
        clientWithHandler {
          respond(
            content = json,
            headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
          )
        }

      val api = GitHubReleasesApi(client, TEST_RELEASES_URL)
      val result = api.getReleases()

      assertEquals(2, result.size)
      assertEquals("v1.2.0", result[0].tagName)
      assertEquals("Release 1.2.0", result[0].name)
      assertEquals("## Changes\n* Added feature X", result[0].body)
      assertEquals("2026-02-15T12:00:00Z", result[0].publishedAt)
      assertEquals("v1.1.0", result[1].tagName)
    }

  @Test
  fun `when getReleases fails then throws exception`() =
    runTest {
      val client =
        HttpClient(MockEngine { throw RuntimeException("Network error") }) {
          install(ContentNegotiation) { json() }
        }

      val api = GitHubReleasesApi(client, TEST_RELEASES_URL)

      val error =
        kotlin.test.assertFailsWith<RuntimeException> {
          api.getReleases()
        }

      assertEquals("Network error", error.message)
    }

  private fun clientWithHandler(
    handler: suspend MockRequestHandleScope.(HttpRequestData) -> io.ktor.client.request.HttpResponseData,
  ): HttpClient =
    HttpClient(MockEngine { request -> handler(request) }) {
      install(ContentNegotiation) {
        json(
          Json {
            ignoreUnknownKeys = true
            isLenient = true
          },
        )
      }
    }
}
