package space.be1ski.memos.shared.data.mapper

import space.be1ski.memos.shared.data.remote.dto.MemoDto
import space.be1ski.memos.shared.domain.model.Memo

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
    createTime = dto.createTime,
    updateTime = dto.updateTime
  )

  /**
   * Converts a list of [MemoDto] into domain [Memo] models.
   */
  fun toDomainList(dtos: List<MemoDto>): List<Memo> = dtos.map(::toDomain)
}
