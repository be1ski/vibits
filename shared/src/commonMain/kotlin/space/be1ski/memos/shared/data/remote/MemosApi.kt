package space.be1ski.memos.shared.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.HttpHeaders
import space.be1ski.memos.shared.data.remote.dto.ListMemosResponseDto

/**
 * Lightweight API client for the Memos service.
 */
class MemosApi(
  private val httpClient: HttpClient
) {
  /**
   * Loads a page of memos from the server.
   */
  suspend fun listMemos(
    baseUrl: String,
    token: String,
    pageSize: Int,
    pageToken: String?
  ): ListMemosResponseDto {
    val normalizedBaseUrl = baseUrl.trim().trimEnd('/')
    return httpClient.get("$normalizedBaseUrl/api/v1/memos") {
      header(HttpHeaders.Authorization, "Bearer $token")
      parameter("pageSize", pageSize)
      parameter("limit", pageSize)
      if (!pageToken.isNullOrBlank()) {
        parameter("pageToken", pageToken)
      }
    }.body()
  }
}
