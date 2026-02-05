package space.be1ski.vibits.feature.homescreen.presentation.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import space.be1ski.vibits.feature.habits.domain.model.ContributionDay
import space.be1ski.vibits.feature.habits.domain.model.HabitConfig
import space.be1ski.vibits.feature.habits.domain.model.HabitsConfigEntry
import space.be1ski.vibits.feature.habits.domain.usecase.ExtractDailyMemosUseCase
import space.be1ski.vibits.feature.habits.domain.usecase.ExtractHabitsConfigUseCase
import space.be1ski.vibits.feature.habits.presentation.view.buildHabitDay
import space.be1ski.vibits.feature.memos.domain.model.Memo

internal class TodayHabits(
  val config: List<HabitConfig>,
  val day: ContributionDay?,
)

@Composable
internal fun rememberTodayHabits(
  habitsTimeline: List<HabitsConfigEntry>,
  memos: List<Memo>,
  timeZone: TimeZone,
  today: LocalDate,
): TodayHabits {
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
  return TodayHabits(config = todayConfig, day = todayDay)
}
