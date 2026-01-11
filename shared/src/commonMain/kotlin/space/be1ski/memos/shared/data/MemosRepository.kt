package space.be1ski.memos.shared.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.HttpHeaders
import space.be1ski.memos.shared.model.ListMemosResponse
import space.be1ski.memos.shared.model.Memo

class MemosRepository(
  private val httpClient: HttpClient
) {
  suspend fun listMemos(baseUrl: String, token: String, limit: Int = 20): List<Memo> {
    val normalizedBaseUrl = baseUrl.trim().trimEnd('/')
    val response: ListMemosResponse = httpClient.get("$normalizedBaseUrl/api/v1/memos") {
      header(HttpHeaders.Authorization, "Bearer $token")
      parameter("limit", limit)
    }.body()
    return response.memos
  }
}
