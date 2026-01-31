package space.be1ski.vibits.shared.feature.sync.domain.usecase

import kotlin.random.Random

/**
 * Generates a temporary memo name for locally created memos.
 * The server will assign a real name during sync.
 */
object GenerateTempMemoNameUseCase {
  private const val PREFIX = "local_"

  operator fun invoke(): String {
    val timestamp = kotlin.time.Clock.System.now().toEpochMilliseconds()
    val random = Random.nextInt(10000, 99999)
    return "$PREFIX${timestamp}_$random"
  }

  fun isTemporaryName(name: String): Boolean = name.startsWith(PREFIX)
}
