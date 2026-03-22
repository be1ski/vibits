package space.be1ski.vibits.feature.changelog.data

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
import kotlin.test.Test
import kotlin.test.assertEquals

class ChangelogRepositoryImplTest {
  @Test
  fun `when getReleases then maps DTOs to domain entries`() =
    runTest {
      val json =
        """
        [
          {
            "tag_name": "v1.2.0",
            "name": "Release 1.2.0",
            "body": "## Changes\n* Feature X",
            "published_at": "2026-02-15T12:00:00Z"
          }
        ]
        """.trimIndent()

      val client =
        HttpClient(MockEngine { respond(json, headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())) }) {
          install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

      val repository = ChangelogRepositoryImpl(GitHubReleasesApi(client, "https://api.github.com/repos/test/repo/releases"))
      val result = repository.getReleases()

      assertEquals(1, result.size)
      assertEquals("1.2.0", result[0].version)
      assertEquals("Release 1.2.0", result[0].title)
      assertEquals("## Changes\n* Feature X", result[0].body)
      assertEquals("2026-02-15", result[0].date)
      assertEquals(false, result[0].hasDmgAsset)
    }

  @Test
  fun `when release has blank name then uses tag name as title`() =
    runTest {
      val json =
        """
        [
          {
            "tag_name": "v1.0.0",
            "name": "",
            "body": "Initial release",
            "published_at": "2026-01-01T00:00:00Z"
          }
        ]
        """.trimIndent()

      val client =
        HttpClient(MockEngine { respond(json, headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())) }) {
          install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

      val repository = ChangelogRepositoryImpl(GitHubReleasesApi(client, "https://api.github.com/repos/test/repo/releases"))
      val result = repository.getReleases()

      assertEquals("v1.0.0", result[0].title)
    }

  @Test
  fun `when release has dmg asset then hasDmgAsset is true`() =
    runTest {
      val json = """
        [
          {
            "tag_name": "v1.2.0",
            "name": "Release 1.2.0",
            "body": "body",
            "published_at": "2026-02-15T12:00:00Z",
            "assets": [
              {"name": "Vibits-1.2.0.dmg"},
              {"name": "Vibits-1.2.0.zip"}
            ]
          }
        ]
      """.trimIndent()
      val client =
        HttpClient(MockEngine { respond(json, headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())) }) {
          install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }
      val repository = ChangelogRepositoryImpl(GitHubReleasesApi(client, "https://api.github.com/repos/test/repo/releases"))
      val result = repository.getReleases()
      assertEquals(true, result[0].hasDmgAsset)
    }

  @Test
  fun `when release has no T in date then uses full date`() =
    runTest {
      val json =
        """
        [
          {
            "tag_name": "v1.0.0",
            "name": "Release",
            "body": "body",
            "published_at": "2026-01-01"
          }
        ]
        """.trimIndent()

      val client =
        HttpClient(MockEngine { respond(json, headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())) }) {
          install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

      val repository = ChangelogRepositoryImpl(GitHubReleasesApi(client, "https://api.github.com/repos/test/repo/releases"))
      val result = repository.getReleases()

      assertEquals("2026-01-01", result[0].date)
    }
}
