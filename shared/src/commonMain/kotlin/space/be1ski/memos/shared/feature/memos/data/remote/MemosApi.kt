package space.be1ski.memos.shared.feature.memos.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import space.be1ski.memos.shared.feature.memos.data.remote.dto.CreateMemoRequestDto
import space.be1ski.memos.shared.feature.memos.data.remote.dto.ListMemosResponseDto
import space.be1ski.memos.shared.feature.memos.data.remote.dto.MemoDto
import space.be1ski.memos.shared.feature.memos.data.remote.dto.UpdateMemoRequestDto

class MemosApi(
  private val httpClient: HttpClient
) {
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

  suspend fun updateMemo(
    baseUrl: String,
    token: String,
    name: String,
    content: String
  ): MemoDto {
    val normalizedBaseUrl = baseUrl.trim().trimEnd('/')
    return httpClient.patch("$normalizedBaseUrl/api/v1/$name") {
      header(HttpHeaders.Authorization, "Bearer $token")
      parameter("updateMask", "content")
      contentType(ContentType.Application.Json)
      setBody(UpdateMemoRequestDto(content = content))
    }.body()
  }

  suspend fun createMemo(
    baseUrl: String,
    token: String,
    content: String
  ): MemoDto {
    val normalizedBaseUrl = baseUrl.trim().trimEnd('/')
    return httpClient.post("$normalizedBaseUrl/api/v1/memos") {
      header(HttpHeaders.Authorization, "Bearer $token")
      contentType(ContentType.Application.Json)
      setBody(CreateMemoRequestDto(content = content))
    }.body()
  }

  suspend fun deleteMemo(
    baseUrl: String,
    token: String,
    name: String
  ) {
    val normalizedBaseUrl = baseUrl.trim().trimEnd('/')
    httpClient.delete("$normalizedBaseUrl/api/v1/$name") {
      header(HttpHeaders.Authorization, "Bearer $token")
    }
  }
}
