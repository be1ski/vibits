package space.be1ski.vibits.shared.feature.memos.data.offline

import dev.zacsweers.metro.Inject
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo
import space.be1ski.vibits.shared.feature.memos.domain.repository.MemosRepository
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Repository implementation for offline mode.
 * Stores memos in local JSON file.
 */
@Inject
class OfflineMemosRepository(
  private val storage: OfflineMemoStorage,
) : MemosRepository {
  override suspend fun listMemos(): List<Memo> = loadMemos()

  override suspend fun cachedMemos(): List<Memo> = loadMemos()

  override suspend fun updateMemo(
    name: String,
    content: String,
  ): Memo {
    val data = storage.load()
    val now = Clock.System.now()
    val nowString = now.toString()

    val updatedMemos =
      data.memos.map { dto ->
        if (dto.name == name) {
          dto.copy(content = content, updateTime = nowString)
        } else {
          dto
        }
      }

    storage.save(data.copy(memos = updatedMemos))

    return updatedMemos
      .find { it.name == name }
      ?.let { toDomain(it) }
      ?: Memo(name = name, content = content, updateTime = now)
  }

  @OptIn(ExperimentalUuidApi::class)
  override suspend fun createMemo(content: String): Memo {
    val data = storage.load()
    val now = Clock.System.now()
    val nowString = now.toString()
    val name = "memos/${now.toEpochMilliseconds()}_${Uuid.random()}"

    val newMemo =
      OfflineMemoDto(
        name = name,
        content = content,
        createTime = nowString,
        updateTime = nowString,
      )

    val updatedMemos = data.memos + newMemo
    storage.save(data.copy(memos = updatedMemos))

    return toDomain(newMemo)
  }

  override suspend fun deleteMemo(name: String) {
    val data = storage.load()
    val filteredMemos = data.memos.filter { it.name != name }
    storage.save(data.copy(memos = filteredMemos))
  }

  private fun loadMemos(): List<Memo> {
    val data = storage.load()
    return data.memos.map { toDomain(it) }
  }

  private fun toDomain(dto: OfflineMemoDto): Memo =
    Memo(
      name = dto.name,
      content = dto.content,
      createTime = parseInstant(dto.createTime),
      updateTime = parseInstant(dto.updateTime),
    )

  private fun parseInstant(value: String?): Instant? {
    if (value.isNullOrBlank()) {
      return null
    }
    return runCatching { Instant.parse(value.trim()) }.getOrNull()
  }
}
