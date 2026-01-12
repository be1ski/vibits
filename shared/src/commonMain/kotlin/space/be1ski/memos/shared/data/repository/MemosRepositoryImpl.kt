package space.be1ski.memos.shared.data.repository

import space.be1ski.memos.shared.data.mapper.MemoMapper
import space.be1ski.memos.shared.data.remote.MemosApi
import space.be1ski.memos.shared.data.remote.MemosPagination
import space.be1ski.memos.shared.domain.model.Memo
import space.be1ski.memos.shared.domain.repository.CredentialsRepository
import space.be1ski.memos.shared.domain.repository.MemosRepository
import space.be1ski.memos.shared.domain.config.MemosDefaults

/**
 * Repository implementation that loads memos from the network.
 */
class MemosRepositoryImpl(
  private val memosApi: MemosApi,
  private val memoMapper: MemoMapper,
  private val credentialsRepository: CredentialsRepository
) : MemosRepository {
  /**
   * Loads memos from the server using paginated API calls.
   *
   * @param baseUrl Base URL for Memos server.
   * @param token Access token.
   * @param pageSize Page size for each request.
   * @return Full list of memos collected across pages.
   */
  override suspend fun listMemos(): List<Memo> {
    val credentials = credentialsRepository.load()
    val baseUrl = credentials.baseUrl.trim()
    val token = credentials.token.trim()
    if (baseUrl.isBlank() || token.isBlank()) {
      throw IllegalStateException("Base URL and token are required.")
    }
    val allMemos = mutableListOf<Memo>()
    val seenTokens = mutableSetOf<String>()
    var nextPageToken: String? = null
    var pages = 0

    do {
      val response = memosApi.listMemos(
        baseUrl = baseUrl,
        token = token,
        pageSize = MemosDefaults.DEFAULT_PAGE_SIZE,
        pageToken = nextPageToken
      )
      allMemos += memoMapper.toDomainList(response.memos)
      nextPageToken = response.nextPageToken?.takeIf { it.isNotBlank() }
      nextPageToken?.let { tokenValue ->
        if (!seenTokens.add(tokenValue)) {
          nextPageToken = null
        }
      }
      pages += 1
    } while (nextPageToken != null && pages < MemosPagination.MAX_PAGES && allMemos.isNotEmpty())

    return allMemos
  }

  /**
   * Updates memo content in the API.
   */
  override suspend fun updateMemo(name: String, content: String): Memo {
    val credentials = credentialsRepository.load()
    val baseUrl = credentials.baseUrl.trim()
    val token = credentials.token.trim()
    if (baseUrl.isBlank() || token.isBlank()) {
      throw IllegalStateException("Base URL and token are required.")
    }
    val dto = memosApi.updateMemo(
      baseUrl = baseUrl,
      token = token,
      name = name,
      content = content
    )
    return memoMapper.toDomain(dto)
  }

  /**
   * Creates a new memo in the API.
   */
  override suspend fun createMemo(content: String): Memo {
    val credentials = credentialsRepository.load()
    val baseUrl = credentials.baseUrl.trim()
    val token = credentials.token.trim()
    if (baseUrl.isBlank() || token.isBlank()) {
      throw IllegalStateException("Base URL and token are required.")
    }
    val dto = memosApi.createMemo(
      baseUrl = baseUrl,
      token = token,
      content = content
    )
    return memoMapper.toDomain(dto)
  }

  /**
   * Deletes a memo in the API.
   */
  override suspend fun deleteMemo(name: String) {
    val credentials = credentialsRepository.load()
    val baseUrl = credentials.baseUrl.trim()
    val token = credentials.token.trim()
    if (baseUrl.isBlank() || token.isBlank()) {
      throw IllegalStateException("Base URL and token are required.")
    }
    memosApi.deleteMemo(
      baseUrl = baseUrl,
      token = token,
      name = name
    )
  }
}
