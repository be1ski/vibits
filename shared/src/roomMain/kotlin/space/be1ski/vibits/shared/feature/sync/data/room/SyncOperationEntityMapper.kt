package space.be1ski.vibits.shared.feature.sync.data.room

import space.be1ski.vibits.shared.feature.sync.domain.model.SyncOperation
import space.be1ski.vibits.shared.feature.sync.domain.model.SyncOperationStatus
import space.be1ski.vibits.shared.feature.sync.domain.model.SyncOperationType
import kotlin.time.Instant

/**
 * Maps sync operation entities to domain models and back.
 */
object SyncOperationEntityMapper {
  /**
   * Converts an entity to a domain model.
   */
  fun toDomain(entity: SyncOperationEntity): SyncOperation =
    SyncOperation(
      id = entity.id,
      type = SyncOperationType.valueOf(entity.type),
      memoName = entity.memoName,
      content = entity.content,
      createdAt = Instant.fromEpochMilliseconds(entity.createdAtMillis),
      status = SyncOperationStatus.valueOf(entity.status),
    )

  /**
   * Converts a domain model to an entity.
   */
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
