package space.be1ski.vibits.feature.sync.domain.model

import kotlin.random.Random

private const val RANDOM_MIN = 100000000
private const val RANDOM_MAX = 999999999

/**
 * Utility for temporary memo names used for locally created memos.
 * The server assigns a real name during sync.
 */
object TempMemoName {
  private const val PREFIX = "local_"

  fun generate(): String {
    val timestamp =
      kotlin.time.Clock.System
        .now()
        .toEpochMilliseconds()
    val random = Random.nextInt(RANDOM_MIN, RANDOM_MAX)
    return "$PREFIX${timestamp}_$random"
  }

  fun isTemporary(name: String): Boolean = name.startsWith(PREFIX)
}
