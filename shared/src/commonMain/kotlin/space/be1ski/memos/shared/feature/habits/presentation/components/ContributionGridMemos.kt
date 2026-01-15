package space.be1ski.memos.shared.feature.habits.presentation.components

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.time.Instant as KtInstant
import space.be1ski.memos.shared.feature.habits.domain.model.DailyMemoInfo
import space.be1ski.memos.shared.feature.habits.domain.model.RangeBounds
import space.be1ski.memos.shared.feature.habits.domain.usecase.CountDailyPostsUseCase
import space.be1ski.memos.shared.feature.habits.domain.usecase.ExtractDailyMemosUseCase
import space.be1ski.memos.shared.feature.memos.domain.model.Memo

private val extractDailyMemosUseCase = ExtractDailyMemosUseCase()
private val countDailyPostsUseCase = CountDailyPostsUseCase()

internal fun extractDailyMemos(
  memos: List<Memo>,
  timeZone: TimeZone
): Map<LocalDate, DailyMemoInfo> = extractDailyMemosUseCase(memos, timeZone)

internal fun findDailyMemoForDate(
  memos: List<Memo>,
  timeZone: TimeZone,
  date: LocalDate
): DailyMemoInfo? = extractDailyMemosUseCase.forDate(memos, timeZone, date)

internal fun parseDailyDateFromContent(content: String): LocalDate? =
  ExtractDailyMemosUseCase.parseDailyDateFromContent(content)

internal fun extractDailyPostCounts(
  memos: List<Memo>,
  timeZone: TimeZone,
  bounds: RangeBounds
): Map<LocalDate, Int> = countDailyPostsUseCase(memos, timeZone, bounds)

internal fun parseMemoDate(memo: Memo, timeZone: TimeZone): LocalDate? =
  ExtractDailyMemosUseCase.parseMemoDate(memo, timeZone)

internal fun parseMemoInstant(memo: Memo): KtInstant? =
  ExtractDailyMemosUseCase.parseMemoInstant(memo)
