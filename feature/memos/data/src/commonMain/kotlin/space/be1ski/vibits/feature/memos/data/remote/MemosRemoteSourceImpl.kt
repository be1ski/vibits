package space.be1ski.vibits.feature.memos.data.remote

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import space.be1ski.vibits.core.platform.di.AppScope
import space.be1ski.vibits.feature.auth.domain.model.requireFilled
import space.be1ski.vibits.feature.auth.domain.repository.CredentialsRepository
import space.be1ski.vibits.feature.memos.data.mapper.MemoMapper
import space.be1ski.vibits.feature.memos.domain.model.Memo
import space.be1ski.vibits.feature.memos.domain.repository.MemosPage
import space.be1ski.vibits.feature.memos.domain.repository.MemosRemoteSource

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
internal class MemosRemoteSourceImpl(
  private val memosApi: MemosApi,
  private val credentialsRepository: CredentialsRepository,
) : MemosRemoteSource {
  private fun credentials(): Pair<String, String> {
    val creds = credentialsRepository.load().requireFilled()
    return creds.baseUrl to creds.token
  }

  override suspend fun listMemos(
    pageSize: Int,
    pageToken: String?,
  ): MemosPage {
    val (baseUrl, token) = credentials()
    val response = memosApi.listMemos(baseUrl, token, pageSize, pageToken)
    return MemosPage(
      memos = MemoMapper.toDomainList(response.memos),
      nextPageToken = response.nextPageToken?.takeIf { it.isNotBlank() },
    )
  }

  override suspend fun createMemo(content: String): Memo {
    val (baseUrl, token) = credentials()
    return MemoMapper.toDomain(memosApi.createMemo(baseUrl, token, content))
  }

  override suspend fun updateMemo(
    name: String,
    content: String,
  ): Memo {
    val (baseUrl, token) = credentials()
    return MemoMapper.toDomain(memosApi.updateMemo(baseUrl, token, name, content))
  }

  override suspend fun deleteMemo(name: String) {
    val (baseUrl, token) = credentials()
    memosApi.deleteMemo(baseUrl, token, name)
  }
}
