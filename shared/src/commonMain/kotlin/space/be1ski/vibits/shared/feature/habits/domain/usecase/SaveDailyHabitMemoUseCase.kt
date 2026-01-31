package space.be1ski.vibits.shared.feature.habits.domain.usecase

import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.TimeZone
import space.be1ski.vibits.shared.app.di.AppScope
import space.be1ski.vibits.shared.core.logging.Log
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo
import space.be1ski.vibits.shared.feature.memos.domain.repository.MemosRepository

private const val TAG = "SaveDailyHabitMemo"

/**
 * Result of saving a daily habit memo.
 */
sealed interface SaveDailyMemoResult {
  data class Created(
    val memo: Memo,
  ) : SaveDailyMemoResult

  data class Updated(
    val memo: Memo,
  ) : SaveDailyMemoResult

  data class Error(
    val message: String,
    val exception: Throwable? = null,
  ) : SaveDailyMemoResult
}

/**
 * Atomically saves a daily habit memo, preventing race conditions when
 * multiple habits are toggled rapidly for the same date.
 *
 * This use case ensures that only ONE daily memo exists per date by:
 * 1. Extracting the date from the content
 * 2. Checking if a daily memo already exists for that date
 * 3. Creating a new memo OR updating the existing one
 *
 * Uses a mutex to ensure thread-safety and prevent duplicate creation.
 */
@Inject
@SingleIn(AppScope::class)
class SaveDailyHabitMemoUseCase(
  private val memosRepository: MemosRepository,
) {
  private val mutex = Mutex()

  /**
   * Saves a daily habit memo for the given content.
   * If a daily memo already exists for the date in the content, it will be updated.
   * Otherwise, a new memo will be created.
   *
   * @param content The daily memo content (must contain #habits/daily DATE format)
   * @return Result indicating whether the memo was created, updated, or an error occurred
   */
  suspend operator fun invoke(content: String): SaveDailyMemoResult =
    mutex.withLock {
      val date = parseDailyDateFromContent(content)
      if (date == null) {
        Log.e(TAG, "Failed to parse date from content")
        return@withLock SaveDailyMemoResult.Error("Invalid daily memo content: no date found")
      }

      Log.d(TAG, "Saving daily memo for date: $date")

      runCatching {
        // Get current cached memos to check for existing daily memo
        val cachedMemos = memosRepository.cachedMemos()
        val existingMemo =
          ExtractDailyMemosUseCase.forDate(
            memos = cachedMemos,
            timeZone = TimeZone.currentSystemDefault(),
            date = date,
          )

        if (existingMemo != null) {
          Log.d(TAG, "Found existing daily memo for $date: ${existingMemo.name}, updating")
          val updated = memosRepository.updateMemo(existingMemo.name, content)
          SaveDailyMemoResult.Updated(updated)
        } else {
          Log.d(TAG, "No existing daily memo for $date, creating new one")
          val created = memosRepository.createMemo(content)
          SaveDailyMemoResult.Created(created)
        }
      }.getOrElse { e ->
        Log.e(TAG, "Failed to save daily memo", e)
        SaveDailyMemoResult.Error(e.message ?: "Failed to save memo", e)
      }
    }
}
