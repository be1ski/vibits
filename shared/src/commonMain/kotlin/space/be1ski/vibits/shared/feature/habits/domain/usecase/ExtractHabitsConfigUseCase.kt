package space.be1ski.vibits.shared.feature.habits.domain.usecase

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import space.be1ski.vibits.shared.feature.habits.domain.model.HabitsConfigEntry
import space.be1ski.vibits.shared.feature.habits.domain.parseHabitConfigLine
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo
import space.be1ski.vibits.shared.feature.memos.domain.model.PostTags

/**
 * Extracts habits configuration entries from memos.
 * Config memos are identified by PostTags.HABITS_CONFIG or PostTags.HABITS_CONFIG_ALT tags.
 */
object ExtractHabitsConfigUseCase {
  operator fun invoke(
    memos: List<Memo>,
    timeZone: TimeZone,
  ): List<HabitsConfigEntry> {
    val entries =
      memos.mapNotNull { memo ->
        if (!memo.content.contains(PostTags.HABITS_CONFIG) &&
          !memo.content.contains(PostTags.HABITS_CONFIG_ALT)
        ) {
          return@mapNotNull null
        }
        // Use createTime for config memos to keep date stable when content is edited
        val instant = memo.createTime ?: return@mapNotNull null
        val date = instant.toLocalDateTime(timeZone).date
        val lines =
          memo.content
            .lineSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .filterNot { it.startsWith(PostTags.HABITS_CONFIG) || it.startsWith(PostTags.HABITS_CONFIG_ALT) }
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
