package space.be1ski.vibits.shared.feature.memos.data.local

import kotlin.time.Instant
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo

/**
 * Maps memo entities to domain models and back.
 */
object MemoEntityMapper {
  /**
   * Converts a cached entity into a domain memo.
   */
  fun toDomain(entity: MemoEntity): Memo = Memo(
    name = entity.name,
    content = entity.content,
    createTime = entity.createTimeMillis?.let(Instant::fromEpochMilliseconds),
    updateTime = entity.updateTimeMillis?.let(Instant::fromEpochMilliseconds)
  )

  /**
   * Converts a domain memo into a cached entity.
   */
  fun toEntity(memo: Memo): MemoEntity = MemoEntity(
    name = memo.name,
    content = memo.content,
    createTimeMillis = memo.createTime?.toEpochMilliseconds(),
    updateTimeMillis = memo.updateTime?.toEpochMilliseconds()
  )
}
