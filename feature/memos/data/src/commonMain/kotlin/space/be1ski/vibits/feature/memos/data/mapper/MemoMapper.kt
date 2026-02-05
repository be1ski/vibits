package space.be1ski.vibits.feature.memos.data.mapper

import space.be1ski.vibits.feature.memos.data.remote.dto.MemoDto
import space.be1ski.vibits.feature.memos.domain.model.Memo

object MemoMapper {
  fun toDomain(dto: MemoDto): Memo =
    Memo(
      name = dto.name,
      content = dto.content,
      createTime = parseInstant(dto.createTime),
      updateTime = parseInstant(dto.updateTime),
    )

  fun toDomainList(dtos: List<MemoDto>): List<Memo> = dtos.map(::toDomain)
}
