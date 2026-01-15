package space.be1ski.vibits.shared.feature.habits.domain.model

import kotlinx.datetime.LocalDate
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo

/**
 * Configuration entry with its effective date.
 */
data class HabitsConfigEntry(
  /** Date when config was created. */
  val date: LocalDate,
  /** Habits declared in the config. */
  val habits: List<HabitConfig>,
  /** Source memo. */
  val memo: Memo
)
