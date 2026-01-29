package space.be1ski.vibits.shared.feature.habits.domain.usecase

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone
import space.be1ski.vibits.shared.app.domain.model.ActivityMode
import space.be1ski.vibits.shared.app.domain.model.ActivityRange
import space.be1ski.vibits.shared.core.platform.date.currentLocalDate
import space.be1ski.vibits.shared.feature.habits.domain.model.ActivityWeekData
import space.be1ski.vibits.shared.feature.habits.domain.model.HabitsConfigEntry
import space.be1ski.vibits.shared.feature.habits.domain.model.SuccessRateData
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode

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

data class PrewarmResult(
  val range: ActivityRange,
  val mode: ActivityMode,
  val appMode: AppMode,
  val weekData: ActivityWeekData,
  val configTimeline: List<HabitsConfigEntry>,
  val successRate: SuccessRateData?,
)
