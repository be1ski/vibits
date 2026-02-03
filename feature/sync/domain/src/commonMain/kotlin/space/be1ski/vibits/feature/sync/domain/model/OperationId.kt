package space.be1ski.vibits.feature.sync.domain.model

import kotlin.random.Random

private const val RANDOM_MAX = Int.MAX_VALUE

/**
 * Utility for generating unique sync operation IDs.
 */
object OperationId {
  fun generate(): String {
    val timestamp =
      kotlin.time.Clock.System
        .now()
        .toEpochMilliseconds()
    val random = Random.nextInt(0, RANDOM_MAX)
    return "op_${timestamp}_$random"
  }
}
