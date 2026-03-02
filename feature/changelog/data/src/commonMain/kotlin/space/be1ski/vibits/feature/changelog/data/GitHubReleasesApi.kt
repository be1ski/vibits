package space.be1ski.vibits.feature.changelog.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import space.be1ski.vibits.core.utils.coroutines.runSuspendCatching
import space.be1ski.vibits.core.utils.logging.Log

private const val TAG = "GitHubReleasesApi"

class GitHubReleasesApi(
  private val httpClient: HttpClient,
  private val releasesUrl: String,
) {
  suspend fun getReleases(): List<GitHubReleaseDto> {
    if (releasesUrl.isBlank()) return emptyList()
    Log.i(TAG, "GET $releasesUrl")
    return runSuspendCatching {
      val response: List<GitHubReleaseDto> =
        httpClient
          .get(releasesUrl) {
            header(HttpHeaders.Accept, "application/vnd.github+json")
          }.body()
      Log.i(TAG, "GET $releasesUrl -> OK, ${response.size} releases")
      response
    }.onFailure { e ->
      Log.e(TAG, "GET $releasesUrl -> FAILED", e)
    }.getOrThrow()
  }
}
