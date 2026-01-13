package space.be1ski.memos.shared.data.mapper

import kotlin.time.Instant
import space.be1ski.memos.shared.data.remote.dto.MemoDto
import space.be1ski.memos.shared.domain.model.memo.Memo

/**
 * Maps network memo DTOs into domain models.
 */
class MemoMapper {
  /**
   * Converts a [MemoDto] into a domain [Memo].
   */
  fun toDomain(dto: MemoDto): Memo = Memo(
    name = dto.name,
    content = dto.content,
    createTime = parseInstant(dto.createTime),
    updateTime = parseInstant(dto.updateTime)
  )

  /**
   * Converts a list of [MemoDto] into domain [Memo] models.
   */
  fun toDomainList(dtos: List<MemoDto>): List<Memo> = dtos.map(::toDomain)

  private fun parseInstant(value: String?): Instant? {
    if (value.isNullOrBlank()) {
      return null
    }
    val trimmed = value.trim()
    return runCatching { Instant.parse(trimmed) }.getOrNull()
      ?: runCatching { Instant.parse("${trimmed}Z") }.getOrNull()
      ?: runCatching {
        val number = trimmed.toLong()
        val millis = if (trimmed.length > 10) number else number * 1000
        Instant.fromEpochMilliseconds(millis)
      }.getOrNull()
  }
}
