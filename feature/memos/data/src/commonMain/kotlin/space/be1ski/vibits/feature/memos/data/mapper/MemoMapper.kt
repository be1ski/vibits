package space.be1ski.vibits.feature.memos.data.mapper

import space.be1ski.vibits.feature.memos.data.remote.dto.MemoDto
import space.be1ski.vibits.feature.memos.domain.model.Memo

/**
 * Maps network memo DTOs into domain models.
 */
object MemoMapper {
  /**
   * Converts a [MemoDto] into a domain [Memo].
   */
  fun toDomain(dto: MemoDto): Memo =
    Memo(
      name = dto.name,
      content = dto.content,
      createTime = parseInstant(dto.createTime),
      updateTime = parseInstant(dto.updateTime),
    )

  /**
   * Converts a list of [MemoDto] into domain [Memo] models.
   */
  fun toDomainList(dtos: List<MemoDto>): List<Memo> = dtos.map(::toDomain)
}
