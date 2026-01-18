package space.be1ski.vibits.shared.feature.habits.domain.usecase

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo

/**
 * Returns the earliest memo date in the dataset.
 */
object EarliestMemoDateUseCase {
  operator fun invoke(
    memos: List<Memo>,
    timeZone: TimeZone,
  ): LocalDate? =
    memos
      .mapNotNull { memo ->
        parseDailyDateFromContent(memo.content) ?: parseMemoDate(memo, timeZone)
      }.minOrNull()
}
