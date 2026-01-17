package space.be1ski.vibits.shared.feature.memos.data

import dev.zacsweers.metro.Inject
import space.be1ski.vibits.shared.feature.auth.domain.repository.CredentialsRepository
import space.be1ski.vibits.shared.feature.memos.data.local.MemoCache
import space.be1ski.vibits.shared.feature.memos.data.mapper.MemoMapper
import space.be1ski.vibits.shared.feature.memos.data.remote.MemosApi
import space.be1ski.vibits.shared.feature.memos.data.remote.MemosPagination
import space.be1ski.vibits.shared.feature.memos.domain.config.MemosDefaults
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo
import space.be1ski.vibits.shared.feature.memos.domain.repository.MemosRepository

/**
 * Repository implementation that loads memos from the network and caches them locally.
 */
@Inject
class MemosRepositoryImpl(
  private val memosApi: MemosApi,
  private val memoMapper: MemoMapper,
  private val credentialsRepository: CredentialsRepository,
  private val memoCache: MemoCache,
) : MemosRepository {
  /**
   * Loads memos from the server using stored credentials and paginated API calls.
   */
  override suspend fun listMemos(): List<Memo> {
    val credentials = credentialsRepository.load()
    val baseUrl = credentials.baseUrl.trim()
    val token = credentials.token.trim()
    check(baseUrl.isNotBlank() && token.isNotBlank()) { "Base URL and token are required." }
    val allMemos = mutableListOf<Memo>()
    val seenTokens = mutableSetOf<String>()
    var nextPageToken: String? = null
    var pages = 0

    do {
      val response =
        memosApi.listMemos(
          baseUrl = baseUrl,
          token = token,
          pageSize = MemosDefaults.DEFAULT_PAGE_SIZE,
          pageToken = nextPageToken,
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

    runCatching { memoCache.replaceMemos(allMemos) }
    return allMemos
  }

  /**
   * Loads cached memos from local storage.
   */
  override suspend fun cachedMemos(): List<Memo> = memoCache.readMemos()

  /**
   * Updates memo content in the API.
   */
  override suspend fun updateMemo(
    name: String,
    content: String,
  ): Memo {
    val credentials = credentialsRepository.load()
    val baseUrl = credentials.baseUrl.trim()
    val token = credentials.token.trim()
    check(baseUrl.isNotBlank() && token.isNotBlank()) { "Base URL and token are required." }
    val dto =
      memosApi.updateMemo(
        baseUrl = baseUrl,
        token = token,
        name = name,
        content = content,
      )
    val updated = memoMapper.toDomain(dto)
    runCatching { memoCache.upsertMemo(updated) }
    return updated
  }

  /**
   * Creates a new memo in the API.
   */
  override suspend fun createMemo(content: String): Memo {
    val credentials = credentialsRepository.load()
    val baseUrl = credentials.baseUrl.trim()
    val token = credentials.token.trim()
    check(baseUrl.isNotBlank() && token.isNotBlank()) { "Base URL and token are required." }
    val dto =
      memosApi.createMemo(
        baseUrl = baseUrl,
        token = token,
        content = content,
      )
    val created = memoMapper.toDomain(dto)
    runCatching { memoCache.upsertMemo(created) }
    return created
  }

  /**
   * Deletes a memo in the API.
   */
  override suspend fun deleteMemo(name: String) {
    val credentials = credentialsRepository.load()
    val baseUrl = credentials.baseUrl.trim()
    val token = credentials.token.trim()
    check(baseUrl.isNotBlank() && token.isNotBlank()) { "Base URL and token are required." }
    memosApi.deleteMemo(
      baseUrl = baseUrl,
      token = token,
      name = name,
    )
    runCatching { memoCache.deleteMemo(name) }
  }
}
