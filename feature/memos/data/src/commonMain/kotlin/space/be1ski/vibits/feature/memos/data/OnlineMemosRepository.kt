package space.be1ski.vibits.feature.memos.data

import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import space.be1ski.vibits.core.platform.di.AppScope
import space.be1ski.vibits.core.utils.logging.Log
import space.be1ski.vibits.feature.auth.domain.model.requireFilled
import space.be1ski.vibits.feature.auth.domain.repository.CredentialsRepository
import space.be1ski.vibits.feature.memos.data.mapper.MemoMapper
import space.be1ski.vibits.feature.memos.data.remote.MemosApi
import space.be1ski.vibits.feature.memos.data.remote.MemosPagination
import space.be1ski.vibits.feature.memos.domain.config.MemosDefaults
import space.be1ski.vibits.feature.memos.domain.model.Memo
import space.be1ski.vibits.feature.memos.domain.repository.MemoCache
import space.be1ski.vibits.feature.memos.domain.repository.MemosRepository

private const val TAG = "MemosRepository"

@Inject
@SingleIn(AppScope::class)
class OnlineMemosRepository(
  private val memosApi: MemosApi,
  private val credentialsRepository: CredentialsRepository,
  private val memoCache: MemoCache,
) : MemosRepository {
  override suspend fun listMemos(): List<Memo> {
    val (baseUrl, token) = credentialsRepository.load().requireFilled()
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
      allMemos += MemoMapper.toDomainList(response.memos)
      nextPageToken = response.nextPageToken?.takeIf { it.isNotBlank() }
      nextPageToken?.let { tokenValue ->
        if (!seenTokens.add(tokenValue)) {
          nextPageToken = null
        }
      }
      pages += 1
    } while (nextPageToken != null && pages < MemosPagination.MAX_PAGES && allMemos.isNotEmpty())

    Log.i(TAG, "Loaded ${allMemos.size} memos in $pages pages")
    runCatching { memoCache.replaceMemos(allMemos) }
      .onSuccess { Log.d(TAG, "Cache updated") }
      .onFailure { Log.e(TAG, "Cache update failed", it) }
    return allMemos
  }

  override suspend fun cachedMemos(): List<Memo> {
    val memos = memoCache.readMemos()
    Log.d(TAG, "Read ${memos.size} memos from cache")
    return memos
  }

  override suspend fun updateMemo(
    name: String,
    content: String,
  ): Memo {
    val (baseUrl, token) = credentialsRepository.load().requireFilled()
    val dto =
      memosApi.updateMemo(
        baseUrl = baseUrl,
        token = token,
        name = name,
        content = content,
      )
    val updated = MemoMapper.toDomain(dto)
    runCatching { memoCache.upsertMemo(updated) }
    Log.i(TAG, "Updated memo: $name")
    return updated
  }

  override suspend fun createMemo(content: String): Memo {
    Log.d(TAG, "Creating memo...")
    val (baseUrl, token) = credentialsRepository.load().requireFilled()
    val dto =
      memosApi.createMemo(
        baseUrl = baseUrl,
        token = token,
        content = content,
      )
    val created = MemoMapper.toDomain(dto)
    runCatching { memoCache.upsertMemo(created) }
    Log.i(TAG, "Created memo: ${created.name}")
    return created
  }

  override suspend fun deleteMemo(name: String) {
    Log.d(TAG, "Deleting memo: $name")
    val (baseUrl, token) = credentialsRepository.load().requireFilled()
    memosApi.deleteMemo(
      baseUrl = baseUrl,
      token = token,
      name = name,
    )
    runCatching { memoCache.deleteMemo(name) }
    Log.i(TAG, "Deleted memo: $name")
  }
}
