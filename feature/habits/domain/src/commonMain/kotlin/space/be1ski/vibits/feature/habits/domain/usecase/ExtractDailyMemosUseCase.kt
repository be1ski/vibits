package space.be1ski.vibits.feature.habits.domain.usecase

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import space.be1ski.vibits.feature.habits.domain.model.DailyMemo
import space.be1ski.vibits.feature.memos.domain.model.Memo
import space.be1ski.vibits.feature.memos.domain.model.isDailyMemo

object ExtractDailyMemosUseCase {
  operator fun invoke(
    memos: List<Memo>,
    timeZone: TimeZone,
  ): Map<LocalDate, DailyMemo> {
    val dailyMemos =
      memos.filter { memo ->
        memo.content.isDailyMemo()
      }
    return dailyMemos
      .mapNotNull { memo ->
        val date =
          parseDailyDateFromContent(memo.content)
            ?: parseMemoDate(memo, timeZone)
            ?: return@mapNotNull null
        date to
          DailyMemo(
            name = memo.name,
            content = memo.content,
          )
      }.toMap()
  }

  fun forDate(
    memos: List<Memo>,
    timeZone: TimeZone,
    date: LocalDate,
  ): DailyMemo? = invoke(memos, timeZone)[date]
}
