package space.be1ski.vibits.shared.feature.habits.domain.usecase

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import space.be1ski.vibits.shared.core.logging.Log
import space.be1ski.vibits.shared.feature.habits.domain.model.HabitsConfigEntry
import space.be1ski.vibits.shared.feature.habits.domain.parseHabitConfigLine
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo
import space.be1ski.vibits.shared.feature.memos.domain.model.PostTags

private const val TAG = "ExtractHabitsConfig"

/**
 * Extracts habits configuration entries from memos.
 * Config memos are identified by PostTags.HABITS_CONFIG or PostTags.HABITS_CONFIG_ALT tags.
 */
object ExtractHabitsConfigUseCase {
  operator fun invoke(
    memos: List<Memo>,
    timeZone: TimeZone,
  ): List<HabitsConfigEntry> {
    Log.d(TAG, "Extracting habits config from ${memos.size} memos")
    val entries =
      memos.mapNotNull { memo ->
        if (!memo.content.contains(PostTags.HABITS_CONFIG) &&
          !memo.content.contains(PostTags.HABITS_CONFIG_ALT)
        ) {
          return@mapNotNull null
        }
        Log.d(TAG, "Found config memo: ${memo.name}")
        Log.d(TAG, "Config content:\n${memo.content}")
        val instant = parseMemoInstant(memo) ?: return@mapNotNull null
        val date = parseMemoDate(memo, timeZone) ?: return@mapNotNull null
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
        Log.d(TAG, "Parsed ${habits.size} habits from config: ${habits.map { it.tag }}")
        HabitsConfigEntry(date = date, habits = habits, memo = memo) to instant
      }
    val result =
      entries
        .sortedBy { it.second.toEpochMilliseconds() }
        .map { it.first }
    Log.d(TAG, "Returning ${result.size} config entries")
    return result
  }

  /**
   * Returns the most recent habits config entry at or before [date].
   */
  fun forDate(
    entries: List<HabitsConfigEntry>,
    date: LocalDate,
  ): HabitsConfigEntry? = entries.lastOrNull { entry -> entry.date <= date }
}
