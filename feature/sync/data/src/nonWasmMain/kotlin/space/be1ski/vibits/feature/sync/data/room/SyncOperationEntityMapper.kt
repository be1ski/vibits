package space.be1ski.vibits.feature.sync.data.room

import space.be1ski.vibits.feature.memos.data.room.sync.SyncOperationEntity
import space.be1ski.vibits.feature.sync.domain.model.SyncOperation
import space.be1ski.vibits.feature.sync.domain.model.SyncOperationStatus
import space.be1ski.vibits.feature.sync.domain.model.SyncOperationType
import kotlin.time.Instant

object SyncOperationEntityMapper {
  fun toDomain(entity: SyncOperationEntity): SyncOperation =
    SyncOperation(
      id = entity.id,
      type = SyncOperationType.valueOf(entity.type),
      memoName = entity.memoName,
      content = entity.content,
      createdAt = Instant.fromEpochMilliseconds(entity.createdAtMillis),
      status = SyncOperationStatus.valueOf(entity.status),
    )

  fun toEntity(operation: SyncOperation): SyncOperationEntity =
    SyncOperationEntity(
      id = operation.id,
      type = operation.type.name,
      memoName = operation.memoName,
      content = operation.content,
      createdAtMillis = operation.createdAt.toEpochMilliseconds(),
      status = operation.status.name,
    )
}
