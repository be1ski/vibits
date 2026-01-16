package space.be1ski.vibits.shared.feature.memos.data.remote

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
import space.be1ski.vibits.shared.core.logging.Log
import space.be1ski.vibits.shared.feature.memos.data.remote.dto.CreateMemoRequestDto
import space.be1ski.vibits.shared.feature.memos.data.remote.dto.ListMemosResponseDto
import space.be1ski.vibits.shared.feature.memos.data.remote.dto.MemoDto
import space.be1ski.vibits.shared.feature.memos.data.remote.dto.UpdateMemoRequestDto

private const val TAG = "MemosApi"

@Suppress("TooGenericExceptionCaught")
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
    val fullUrl = "$normalizedBaseUrl/api/v1/memos"
    Log.i(TAG, "GET $fullUrl")
    return try {
      val response: ListMemosResponseDto = httpClient.get(fullUrl) {
        header(HttpHeaders.Authorization, "Bearer $token")
        parameter("pageSize", pageSize)
        parameter("limit", pageSize)
        if (!pageToken.isNullOrBlank()) {
          parameter("pageToken", pageToken)
        }
      }.body()
      Log.i(TAG, "GET $fullUrl -> OK, ${response.memos.size} memos")
      response
    } catch (e: Exception) {
      Log.e(TAG, "GET $fullUrl -> FAILED", e)
      throw e
    }
  }

  suspend fun updateMemo(
    baseUrl: String,
    token: String,
    name: String,
    content: String
  ): MemoDto {
    val normalizedBaseUrl = baseUrl.trim().trimEnd('/')
    val fullUrl = "$normalizedBaseUrl/api/v1/$name"
    Log.i(TAG, "PATCH $fullUrl")
    return try {
      val response: MemoDto = httpClient.patch(fullUrl) {
        header(HttpHeaders.Authorization, "Bearer $token")
        parameter("updateMask", "content")
        contentType(ContentType.Application.Json)
        setBody(UpdateMemoRequestDto(content = content))
      }.body()
      Log.i(TAG, "PATCH $fullUrl -> OK")
      response
    } catch (e: Exception) {
      Log.e(TAG, "PATCH $fullUrl -> FAILED", e)
      throw e
    }
  }

  suspend fun createMemo(
    baseUrl: String,
    token: String,
    content: String
  ): MemoDto {
    val normalizedBaseUrl = baseUrl.trim().trimEnd('/')
    val fullUrl = "$normalizedBaseUrl/api/v1/memos"
    Log.i(TAG, "POST $fullUrl")
    return try {
      val response: MemoDto = httpClient.post(fullUrl) {
        header(HttpHeaders.Authorization, "Bearer $token")
        contentType(ContentType.Application.Json)
        setBody(CreateMemoRequestDto(content = content))
      }.body()
      Log.i(TAG, "POST $fullUrl -> OK")
      response
    } catch (e: Exception) {
      Log.e(TAG, "POST $fullUrl -> FAILED", e)
      throw e
    }
  }

  suspend fun deleteMemo(
    baseUrl: String,
    token: String,
    name: String
  ) {
    val normalizedBaseUrl = baseUrl.trim().trimEnd('/')
    val fullUrl = "$normalizedBaseUrl/api/v1/$name"
    Log.i(TAG, "DELETE $fullUrl")
    try {
      httpClient.delete(fullUrl) {
        header(HttpHeaders.Authorization, "Bearer $token")
      }
      Log.i(TAG, "DELETE $fullUrl -> OK")
    } catch (e: Exception) {
      Log.e(TAG, "DELETE $fullUrl -> FAILED", e)
      throw e
    }
  }
}
