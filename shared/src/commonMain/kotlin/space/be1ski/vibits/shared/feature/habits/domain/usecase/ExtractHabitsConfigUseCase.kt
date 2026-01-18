package space.be1ski.vibits.shared.feature.habits.domain.usecase

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import space.be1ski.vibits.shared.feature.habits.domain.model.HabitsConfigEntry
import space.be1ski.vibits.shared.feature.habits.domain.parseHabitConfigLine
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo

/**
 * Extracts habits configuration entries from memos.
 * Config memos are identified by #habits/config or #habits_config tags.
 */
object ExtractHabitsConfigUseCase {
  operator fun invoke(
    memos: List<Memo>,
    timeZone: TimeZone,
  ): List<HabitsConfigEntry> {
    val entries =
      memos.mapNotNull { memo ->
        if (!memo.content.contains("#habits/config") && !memo.content.contains("#habits_config")) {
          return@mapNotNull null
        }
        val instant = parseMemoInstant(memo) ?: return@mapNotNull null
        val date = parseMemoDate(memo, timeZone) ?: return@mapNotNull null
        val lines =
          memo.content
            .lineSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .filterNot { it.startsWith("#habits/config") || it.startsWith("#habits_config") }
        val habits =
          lines
            .mapNotNull { line -> parseHabitConfigLine(line) }
            .distinctBy { it.tag }
            .toList()
        HabitsConfigEntry(date = date, habits = habits, memo = memo) to instant
      }
    return entries
      .sortedBy { it.second.toEpochMilliseconds() }
      .map { it.first }
  }

  /**
   * Returns the most recent habits config entry at or before [date].
   */
  fun forDate(
    entries: List<HabitsConfigEntry>,
    date: LocalDate,
  ): HabitsConfigEntry? = entries.lastOrNull { entry -> entry.date <= date }
}
