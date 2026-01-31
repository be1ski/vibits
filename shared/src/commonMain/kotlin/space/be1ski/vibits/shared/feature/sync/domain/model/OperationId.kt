package space.be1ski.vibits.shared.feature.sync.domain.model

import kotlin.random.Random

private const val RANDOM_MIN = 10000
private const val RANDOM_MAX = 99999

/**
 * Utility for generating unique sync operation IDs.
 */
object OperationId {
  fun generate(): String {
    val timestamp =
      kotlin.time.Clock.System
        .now()
        .toEpochMilliseconds()
    val random = Random.nextInt(RANDOM_MIN, RANDOM_MAX)
    return "op_${timestamp}_$random"
  }
}
