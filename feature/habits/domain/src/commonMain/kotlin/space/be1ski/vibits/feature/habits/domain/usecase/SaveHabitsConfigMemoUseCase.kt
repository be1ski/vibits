package space.be1ski.vibits.feature.habits.domain.usecase

import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.datetime.TimeZone
import space.be1ski.vibits.core.platform.date.currentLocalDate
import space.be1ski.vibits.core.platform.di.AppScope
import space.be1ski.vibits.core.utils.coroutines.runSuspendCatching
import space.be1ski.vibits.core.utils.logging.Log
import space.be1ski.vibits.feature.habits.domain.model.SaveConfigMemoResult
import space.be1ski.vibits.feature.memos.domain.repository.MemosRepository

private const val TAG = "SaveHabitsConfigMemo"

@Inject
@SingleIn(AppScope::class)
class SaveHabitsConfigMemoUseCase(
  private val memosRepository: MemosRepository,
) {
  suspend operator fun invoke(content: String): SaveConfigMemoResult =
    runSuspendCatching {
      val timeZone = TimeZone.currentSystemDefault()
      val today = currentLocalDate()
      val cachedMemos = memosRepository.cachedMemos()
      val configEntries = ExtractHabitsConfigUseCase(cachedMemos, timeZone)
      val todayEntry = configEntries.lastOrNull { it.date == today }

      if (todayEntry != null) {
        Log.d(TAG, "Found today's config memo: ${todayEntry.memo.name}, updating")
        val updated = memosRepository.updateMemo(todayEntry.memo.name, content)
        SaveConfigMemoResult.Updated(updated)
      } else {
        Log.d(TAG, "No config memo for today, creating new one")
        val created = memosRepository.createMemo(content)
        SaveConfigMemoResult.Created(created)
      }
    }.getOrElse { e ->
      Log.e(TAG, "Failed to save config memo", e)
      SaveConfigMemoResult.Error(e.message ?: "Failed to save config memo", e)
    }
}
