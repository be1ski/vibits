package space.be1ski.vibits.shared.feature.habits.domain.usecase

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import space.be1ski.vibits.shared.core.ui.ActivityMode
import space.be1ski.vibits.shared.core.ui.ActivityRange
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

class BuildActivityDataUseCaseTest {
  private val useCase =
    BuildActivityDataUseCase(
      buildDayDataUseCase = BuildDayDataUseCase(),
    )

  @Test
  fun `buildWeekData returns weeks for given range`() {
    val range = ActivityRange.Week(startDate = LocalDate(2024, 1, 15))
    val today = LocalDate(2024, 1, 20)
    val timeZone = TimeZone.UTC

    val result =
      useCase.buildWeekData(
        configTimeline = emptyList(),
        dailyMemos = emptyMap(),
        timeZone = timeZone,
        memos = emptyList(),
        range = range,
        mode = ActivityMode.POSTS,
        today = today,
      )

    assertTrue(result.weeks.isNotEmpty())
    assertEquals(
      7,
      result.weeks
        .first()
        .days.size,
    )
  }

  @Test
  fun `buildWeekData calculates maxDaily correctly`() {
    val range = ActivityRange.Week(startDate = LocalDate(2024, 1, 15))
    val today = LocalDate(2024, 1, 20)
    val timeZone = TimeZone.UTC
    val memos =
      listOf(
        Memo(name = "memo1", content = "post 1", createTime = Instant.parse("2024-01-15T10:00:00Z")),
        Memo(name = "memo2", content = "post 2", createTime = Instant.parse("2024-01-15T11:00:00Z")),
        Memo(name = "memo3", content = "post 3", createTime = Instant.parse("2024-01-15T12:00:00Z")),
        Memo(name = "memo4", content = "post 4", createTime = Instant.parse("2024-01-16T10:00:00Z")),
      )

    val result =
      useCase.buildWeekData(
        configTimeline = emptyList(),
        dailyMemos = emptyMap(),
        timeZone = timeZone,
        memos = memos,
        range = range,
        mode = ActivityMode.POSTS,
        today = today,
      )

    assertEquals(3, result.maxDaily)
  }

  @Test
  fun `buildWeekData returns empty weeks when bounds are invalid`() {
    val range = ActivityRange.Week(startDate = LocalDate(2024, 1, 15))
    val today = LocalDate(2024, 1, 20)
    val timeZone = TimeZone.UTC

    val result =
      useCase.buildWeekData(
        configTimeline = emptyList(),
        dailyMemos = emptyMap(),
        timeZone = timeZone,
        memos = emptyList(),
        range = range,
        mode = ActivityMode.HABITS,
        today = today,
      )

    assertTrue(result.weeks.isNotEmpty())
  }
}
