package space.be1ski.vibits.feature.habits.domain.usecase

import dev.zacsweers.metro.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone
import space.be1ski.vibits.core.platform.date.currentLocalDate
import space.be1ski.vibits.core.platform.mode.AppMode
import space.be1ski.vibits.feature.habits.domain.model.ActivityMode
import space.be1ski.vibits.feature.habits.domain.model.PrewarmResult
import space.be1ski.vibits.feature.memos.domain.model.Memo

@Inject
class PrewarmActivityDataUseCase(
  private val calculateActivityDataUseCase: CalculateActivityDataUseCase,
) {
  suspend operator fun invoke(
    memos: List<Memo>,
    appMode: AppMode,
  ): List<PrewarmResult> {
    val timeZone = TimeZone.currentSystemDefault()
    val earliestDate = EarliestMemoDateUseCase(memos, timeZone) ?: return emptyList()
    val today = currentLocalDate()

    val ranges = GenerateActivityRangesUseCase(earliestDate, today)
    val modes = listOf(ActivityMode.HABITS, ActivityMode.POSTS)

    return withContext(Dispatchers.Default) {
      ranges
        .flatMap { range ->
          modes.map { mode ->
            async {
              val data = calculateActivityDataUseCase(range, mode, memos)
              PrewarmResult(range, mode, appMode, data.weekData, data.configTimeline, data.successRate)
            }
          }
        }.awaitAll()
    }
  }
}
