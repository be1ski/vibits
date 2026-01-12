package space.be1ski.memos.shared.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.HttpHeaders
import space.be1ski.memos.shared.config.DEFAULT_PAGE_SIZE
import space.be1ski.memos.shared.config.MAX_PAGES
import space.be1ski.memos.shared.model.ListMemosResponse
import space.be1ski.memos.shared.model.Memo

/**
 * Repository for loading memos from the API.
 */
class MemosRepository(
  private val httpClient: HttpClient
) {
  /**
   * Loads memos from the server using paginated API calls.
   *
   * @param baseUrl Base URL for Memos server.
   * @param token Access token.
   * @param pageSize Page size for each request.
   * @return Full list of memos collected across pages.
   */
  suspend fun listMemos(baseUrl: String, token: String, pageSize: Int = DEFAULT_PAGE_SIZE): List<Memo> {
    val normalizedBaseUrl = baseUrl.trim().trimEnd('/')
    val allMemos = mutableListOf<Memo>()
    val seenTokens = mutableSetOf<String>()
    var nextPageToken: String? = null
    var pages = 0

    do {
      val response: ListMemosResponse = httpClient.get("$normalizedBaseUrl/api/v1/memos") {
        header(HttpHeaders.Authorization, "Bearer $token")
        parameter("pageSize", pageSize)
        parameter("limit", pageSize)
        if (!nextPageToken.isNullOrBlank()) {
          parameter("pageToken", nextPageToken)
        }
      }.body()
      allMemos += response.memos
      nextPageToken = response.nextPageToken?.takeIf { it.isNotBlank() }
      nextPageToken?.let { tokenValue ->
        if (!seenTokens.add(tokenValue)) {
          nextPageToken = null
        }
      }
      pages += 1
    } while (nextPageToken != null && pages < MAX_PAGES && allMemos.isNotEmpty())

    return allMemos
  }
}
