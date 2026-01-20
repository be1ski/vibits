package space.be1ski.vibits.shared.app.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import space.be1ski.vibits.shared.feature.habits.domain.model.ContributionDay
import space.be1ski.vibits.shared.feature.habits.domain.model.HabitConfig
import space.be1ski.vibits.shared.feature.habits.domain.model.HabitsConfigEntry
import space.be1ski.vibits.shared.feature.habits.domain.usecase.ExtractDailyMemosUseCase
import space.be1ski.vibits.shared.feature.habits.domain.usecase.ExtractHabitsConfigUseCase
import space.be1ski.vibits.shared.feature.habits.view.buildHabitDay
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo

internal class TodayData(
  val config: List<HabitConfig>,
  val day: ContributionDay?,
)

@Composable
internal fun rememberTodayData(
  habitsTimeline: List<HabitsConfigEntry>,
  memos: List<Memo>,
  timeZone: TimeZone,
  today: LocalDate,
): TodayData {
  val todayConfig =
    remember(habitsTimeline, today) {
      ExtractHabitsConfigUseCase.forDate(habitsTimeline, today)?.habits.orEmpty()
    }
  val todayMemo =
    remember(memos, today) {
      ExtractDailyMemosUseCase.forDate(memos, timeZone, today)
    }
  val todayDay =
    remember(todayConfig, todayMemo, today) {
      buildHabitDay(date = today, habitsConfig = todayConfig, dailyMemo = todayMemo)
    }
  return TodayData(config = todayConfig, day = todayDay)
}
