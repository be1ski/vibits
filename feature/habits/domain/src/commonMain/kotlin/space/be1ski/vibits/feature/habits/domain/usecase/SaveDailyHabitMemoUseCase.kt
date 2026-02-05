package space.be1ski.vibits.feature.habits.domain.usecase

import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import space.be1ski.vibits.core.platform.di.AppScope
import space.be1ski.vibits.core.utils.logging.Log
import space.be1ski.vibits.feature.habits.domain.buildDailyContent
import space.be1ski.vibits.feature.habits.domain.extractCompletedHabits
import space.be1ski.vibits.feature.habits.domain.model.HabitConfig
import space.be1ski.vibits.feature.habits.domain.model.SaveDailyMemoResult
import space.be1ski.vibits.feature.memos.domain.repository.MemosRepository

private const val TAG = "SaveDailyHabitMemo"

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

  /**
   * Atomically toggles a habit for a specific date.
   * Reads current memo state, applies the toggle, and saves.
   * This ensures correct behavior even with rapid sequential toggles.
   *
   * @param date The date to toggle the habit for
   * @param habitTag The habit tag to toggle
   * @param habitsConfig All habit configurations (for building content)
   * @return Result indicating the operation outcome
   */
  suspend fun toggleHabit(
    date: LocalDate,
    habitTag: String,
    habitsConfig: List<HabitConfig>,
  ): SaveDailyMemoResult =
    mutex.withLock {
      Log.d(TAG, "Toggling habit $habitTag for date $date")

      runCatching {
        // Get current cached memos
        val cachedMemos = memosRepository.cachedMemos()
        val existingMemoInfo =
          ExtractDailyMemosUseCase.forDate(
            memos = cachedMemos,
            timeZone = TimeZone.currentSystemDefault(),
            date = date,
          )

        // Get current completed habits from existing memo
        val currentlyDone =
          if (existingMemoInfo != null) {
            extractCompletedHabits(
              existingMemoInfo.content,
              habitsConfig.map { it.tag }.toSet(),
            )
          } else {
            emptySet()
          }

        // Toggle the specific habit
        val isCurrentlyDone = habitTag in currentlyDone
        val newDone =
          if (isCurrentlyDone) {
            currentlyDone - habitTag
          } else {
            currentlyDone + habitTag
          }

        Log.d(TAG, "Habit $habitTag: $isCurrentlyDone -> ${!isCurrentlyDone}, total done: ${newDone.size}")

        // Build selections map for content builder
        val selections = habitsConfig.associate { it.tag to (it.tag in newDone) }
        val hasAnySelection = selections.values.any { it }

        when {
          !hasAnySelection && existingMemoInfo != null -> {
            // All habits unchecked - delete the memo
            Log.d(TAG, "All habits unchecked, deleting memo: ${existingMemoInfo.name}")
            memosRepository.deleteMemo(existingMemoInfo.name)
            SaveDailyMemoResult.Deleted(existingMemoInfo.name)
          }
          hasAnySelection -> {
            // Build and save the memo
            val content = buildDailyContent(date, habitsConfig, selections)
            if (existingMemoInfo != null) {
              Log.d(TAG, "Updating existing memo: ${existingMemoInfo.name}")
              val updated = memosRepository.updateMemo(existingMemoInfo.name, content)
              SaveDailyMemoResult.Updated(updated)
            } else {
              Log.d(TAG, "Creating new memo for date $date")
              val created = memosRepository.createMemo(content)
              SaveDailyMemoResult.Created(created)
            }
          }
          else -> {
            // No selection and no existing memo - nothing to do
            Log.d(TAG, "No habits selected and no existing memo, nothing to do")
            SaveDailyMemoResult.Error("No changes to save")
          }
        }
      }.getOrElse { e ->
        Log.e(TAG, "Failed to toggle habit", e)
        SaveDailyMemoResult.Error(e.message ?: "Failed to toggle habit", e)
      }
    }
}
