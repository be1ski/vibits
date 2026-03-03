package space.be1ski.vibits.feature.habits.presentation.view.components

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import space.be1ski.vibits.core.ui.date.DateFormatter
import space.be1ski.vibits.feature.habits.domain.model.ActivityRange
import space.be1ski.vibits.feature.habits.domain.model.ActivityWeek
import space.be1ski.vibits.feature.habits.domain.model.ContributionDay
import kotlin.test.Test
import kotlin.test.assertEquals

class ContributionGridTimelineTest {
  private val formatter =
    DateFormatter(
      months =
        mapOf(
          Month.JANUARY to "Jan",
          Month.FEBRUARY to "Feb",
          Month.MARCH to "Mar",
          Month.APRIL to "Apr",
          Month.MAY to "May",
          Month.JUNE to "Jun",
          Month.JULY to "Jul",
          Month.AUGUST to "Aug",
          Month.SEPTEMBER to "Sep",
          Month.OCTOBER to "Oct",
          Month.NOVEMBER to "Nov",
          Month.DECEMBER to "Dec",
        ),
      days =
        mapOf(
          DayOfWeek.MONDAY to "Mo",
          DayOfWeek.TUESDAY to "Tu",
          DayOfWeek.WEDNESDAY to "We",
          DayOfWeek.THURSDAY to "Th",
          DayOfWeek.FRIDAY to "Fr",
          DayOfWeek.SATURDAY to "Sa",
          DayOfWeek.SUNDAY to "Su",
        ),
    )

  @Test
  fun `when quarter starts mid-week then first label uses first in-range month`() {
    // Q1 2026: week starts Mon Dec 29, but Jan 1 is first in-range day
    val weeks =
      listOf(
        week(LocalDate(2025, 12, 29), inRangeFrom = LocalDate(2026, 1, 1)),
        week(LocalDate(2026, 1, 5), inRangeFrom = LocalDate(2026, 1, 5)),
        week(LocalDate(2026, 1, 12), inRangeFrom = LocalDate(2026, 1, 12)),
      )
    val labels = buildTimelineLabels(weeks, ActivityRange.Quarter(2026, 1), formatter)

    assertEquals("J", labels[0])
    assertEquals("", labels[1])
    assertEquals("", labels[2])
  }

  @Test
  fun `when quarter ends mid-week then last label uses last in-range month`() {
    // Q1 2026 ends Mar 31; last week Mon Mar 30 - Sun Apr 5
    val weeks =
      listOf(
        week(LocalDate(2026, 3, 23), inRangeFrom = LocalDate(2026, 3, 23)),
        week(
          LocalDate(2026, 3, 30),
          inRangeFrom = LocalDate(2026, 3, 30),
          inRangeTo = LocalDate(2026, 3, 31),
        ),
      )
    val labels = buildTimelineLabels(weeks, ActivityRange.Quarter(2026, 1), formatter)

    assertEquals("M", labels[0])
    assertEquals("", labels[1])
  }

  @Test
  fun `when month transition within quarter then label appears at correct week`() {
    val weeks =
      listOf(
        week(LocalDate(2026, 1, 26), inRangeFrom = LocalDate(2026, 1, 26)),
        // Feb starts Thu Feb 5 in this week
        week(LocalDate(2026, 2, 2), inRangeFrom = LocalDate(2026, 2, 2)),
        week(LocalDate(2026, 2, 9), inRangeFrom = LocalDate(2026, 2, 9)),
      )
    val labels = buildTimelineLabels(weeks, ActivityRange.Quarter(2026, 1), formatter)

    assertEquals("J", labels[0])
    assertEquals("F", labels[1])
    assertEquals("", labels[2])
  }

  @Test
  fun `when empty weeks then returns empty list`() {
    val labels = buildTimelineLabels(emptyList(), ActivityRange.Quarter(2026, 1), formatter)

    assertEquals(emptyList(), labels)
  }

  @Test
  fun `when year range then quarter start uses in-range date`() {
    // Year 2026: first week starts Mon Dec 29 2025
    val weeks =
      listOf(
        week(LocalDate(2025, 12, 29), inRangeFrom = LocalDate(2026, 1, 1)),
        week(LocalDate(2026, 1, 12), inRangeFrom = LocalDate(2026, 1, 12)),
      )
    val labels = buildTimelineLabels(weeks, ActivityRange.Year(2026), formatter)

    assertEquals("1", labels[0])
    assertEquals("", labels[1])
  }

  @Test
  fun `when month range starts mid-week then first label uses in-range month`() {
    // Nov 2024: first week starts Mon Oct 28, but Nov 1 is first in-range
    val weeks =
      listOf(
        week(LocalDate(2024, 10, 28), inRangeFrom = LocalDate(2024, 11, 1)),
        week(LocalDate(2024, 11, 4), inRangeFrom = LocalDate(2024, 11, 4)),
      )
    val labels = buildTimelineLabels(weeks, ActivityRange.Month(2024, Month.NOVEMBER), formatter)

    assertEquals("N", labels[0])
    assertEquals("", labels[1])
  }

  private fun week(
    startDate: LocalDate,
    inRangeFrom: LocalDate,
    inRangeTo: LocalDate? = null,
  ): ActivityWeek {
    val days =
      (0 until 7).map { offset ->
        val date = LocalDate.fromEpochDays(startDate.toEpochDays() + offset)
        day(date, inRange = date >= inRangeFrom && (inRangeTo == null || date <= inRangeTo))
      }
    return ActivityWeek(
      startDate = startDate,
      days = days,
      weeklyCount = 0,
    )
  }

  private fun day(
    date: LocalDate,
    inRange: Boolean,
  ) = ContributionDay(
    date = date,
    count = 0,
    totalHabits = 0,
    completionRatio = 0f,
    habitStatuses = emptyList(),
    dailyMemo = null,
    inRange = inRange,
  )
}
