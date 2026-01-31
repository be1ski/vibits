package space.be1ski.vibits.shared.feature.sync.domain.usecase

import kotlin.random.Random

/**
 * Generates a unique ID for sync operations.
 */
object GenerateOperationIdUseCase {
  operator fun invoke(): String {
    val timestamp = kotlin.time.Clock.System.now().toEpochMilliseconds()
    val random = Random.nextInt(10000, 99999)
    return "op_${timestamp}_$random"
  }
}
