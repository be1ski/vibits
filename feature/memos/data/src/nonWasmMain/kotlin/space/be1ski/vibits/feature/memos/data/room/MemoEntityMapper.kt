package space.be1ski.vibits.feature.memos.data.room

import space.be1ski.vibits.feature.memos.domain.model.Memo
import kotlin.time.Instant

object MemoEntityMapper {
  fun toDomain(entity: MemoEntity): Memo =
    Memo(
      name = entity.name,
      content = entity.content,
      createTime = entity.createTimeMillis?.let(Instant::fromEpochMilliseconds),
      updateTime = entity.updateTimeMillis?.let(Instant::fromEpochMilliseconds),
    )

  fun toEntity(memo: Memo): MemoEntity =
    MemoEntity(
      name = memo.name,
      content = memo.content,
      createTimeMillis = memo.createTime?.toEpochMilliseconds(),
      updateTimeMillis = memo.updateTime?.toEpochMilliseconds(),
    )
}
