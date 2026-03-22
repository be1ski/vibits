package space.be1ski.vibits.feature.habits.presentation

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import space.be1ski.vibits.feature.habits.domain.model.ActivitySummary
import space.be1ski.vibits.feature.habits.domain.model.ActivityWeek
import space.be1ski.vibits.feature.habits.domain.model.ContributionDay
import space.be1ski.vibits.feature.habits.domain.model.HabitStatus
import space.be1ski.vibits.feature.habits.presentation.reducer.WeekdayStatsSelector
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WeekdayStatsSelectorTest {

  private val tag = "#habits/test"

  // ── Ordering ────────────────────────────────────────────────────────────────

  @Test
  fun `stats are returned in Mon to Sun order`() {
    val summary = twoWeeks(List(7) { true }, List(7) { true })
    val result = WeekdayStatsSelector(summary, tag)
    assertEquals(DayOfWeek.entries, result.stats.map { it.dayOfWeek })
  }

  // ── Rate calculation ─────────────────────────────────────────────────────────

  @Test
  fun `completion rate is calculated correctly per weekday`() {
    // Week1: all done. Week2: Mon and Wed done, rest not done.
    val summary = twoWeeks(
      List(7) { true },
      listOf(true, false, true, false, false, false, false),
    )
    val result = WeekdayStatsSelector(summary, tag)
    assertTrue(result.hasSufficientData)
    val rates = result.stats.associate { it.dayOfWeek to it.completionRate }
    assertEquals(1.0f, rates[DayOfWeek.MONDAY])
    assertEquals(0.5f, rates[DayOfWeek.TUESDAY])
    assertEquals(1.0f, rates[DayOfWeek.WEDNESDAY])
    assertEquals(0.5f, rates[DayOfWeek.THURSDAY])
    assertEquals(0.5f, rates[DayOfWeek.FRIDAY])
    assertEquals(0.5f, rates[DayOfWeek.SATURDAY])
    assertEquals(0.5f, rates[DayOfWeek.SUNDAY])
  }

  // ── Best/worst identification ─────────────────────────────────────────────────

  @Test
  fun `best day is correctly identified when single clear winner`() {
    // Mon: 2/2=1.0. All others: 1/2=0.5.
    val summary = twoWeeks(List(7) { true }, listOf(true, false, false, false, false, false, false))
    val result = WeekdayStatsSelector(summary, tag)
    assertTrue(result.hasSufficientData)
    val best = result.stats.filter { it.isBest }
    assertEquals(1, best.size)
    assertEquals(DayOfWeek.MONDAY, best.single().dayOfWeek)
  }

  @Test
  fun `worst day is correctly identified when single clear loser`() {
    // Mon: 0/2=0.0. All others: 1/2=0.5 or better.
    val summary = twoWeeks(List(7) { true }, listOf(false, true, true, true, true, true, true))
    val result = WeekdayStatsSelector(summary, tag)
    assertTrue(result.hasSufficientData)
    val worst = result.stats.filter { it.isWorst }
    assertEquals(1, worst.size)
    assertEquals(DayOfWeek.MONDAY, worst.single().dayOfWeek)
  }

  // ── Tie-breaking ─────────────────────────────────────────────────────────────

  @Test
  fun `when two days tie for best then neither is marked isBest`() {
    // Mon and Tue: 1.0. All others: 0.5.
    val summary = twoWeeks(List(7) { true }, listOf(true, true, false, false, false, false, false))
    val result = WeekdayStatsSelector(summary, tag)
    assertTrue(result.hasSufficientData)
    assertFalse(result.stats.any { it.isBest })
  }

  @Test
  fun `when two days tie for worst then neither is marked isWorst`() {
    // Mon and Tue: 0.0. All others: 0.5 or better.
    val summary = twoWeeks(List(7) { true }, listOf(false, false, true, true, true, true, true))
    val result = WeekdayStatsSelector(summary, tag)
    assertTrue(result.hasSufficientData)
    assertFalse(result.stats.any { it.isWorst })
  }

  @Test
  fun `when all days have equal non-zero rate then no highlights`() {
    val summary = twoWeeks(List(7) { true }, List(7) { true })
    val result = WeekdayStatsSelector(summary, tag)
    assertTrue(result.hasSufficientData)
    assertFalse(result.stats.any { it.isBest || it.isWorst })
  }

  @Test
  fun `when all days have zero rate then no highlights`() {
    val summary = twoWeeks(List(7) { false }, List(7) { false })
    val result = WeekdayStatsSelector(summary, tag)
    assertTrue(result.hasSufficientData)
    assertFalse(result.stats.any { it.isBest || it.isWorst })
  }

  // ── Invariant ─────────────────────────────────────────────────────────────────

  @Test
  fun `no entry has both isBest and isWorst true`() {
    val summary = twoWeeks(List(7) { true }, listOf(true, false, true, false, true, false, true))
    val result = WeekdayStatsSelector(summary, tag)
    assertTrue(result.stats.none { it.isBest && it.isWorst })
  }

  // ── Insufficient data ────────────────────────────────────────────────────────

  @Test
  fun `when any weekday has fewer than 2 observations then hasSufficientData is false and no highlights`() {
    // Only 1 week of data → each weekday has 1 observation.
    val week = listOf(date(1), date(2), date(3), date(4), date(5), date(6), date(7))
    val summary = summaryOf(week.map { obs(it, true) })
    val result = WeekdayStatsSelector(summary, tag)
    assertFalse(result.hasSufficientData)
    assertFalse(result.stats.any { it.isBest || it.isWorst })
    assertNull(result.averageCompletionRate)
  }

  // ── Observation filtering ────────────────────────────────────────────────────

  @Test
  fun `when out-of-range days are present then they are excluded`() {
    val w1 = listOf(date(1), date(2), date(3), date(4), date(5), date(6), date(7))
    val w2 = listOf(date(8), date(9), date(10), date(11), date(12), date(13), date(14))
    val summary = summaryOf(w1.map { obs(it, true) }, w2.map { outOfRange(it) })
    // Only 1 observation per weekday after exclusion → insufficient data
    val result = WeekdayStatsSelector(summary, tag)
    assertFalse(result.hasSufficientData)
  }

  @Test
  fun `when future days are present then they are excluded`() {
    val w1 = listOf(date(1), date(2), date(3), date(4), date(5), date(6), date(7))
    val w2 = listOf(date(8), date(9), date(10), date(11), date(12), date(13), date(14))
    val summary = summaryOf(w1.map { obs(it, true) }, w2.map { futureDay(it) })
    val result = WeekdayStatsSelector(summary, tag)
    assertFalse(result.hasSufficientData)
  }

  @Test
  fun `when pre-config days have no matching habit status then they are excluded`() {
    val w1 = listOf(date(1), date(2), date(3), date(4), date(5), date(6), date(7))
    val w2 = listOf(date(8), date(9), date(10), date(11), date(12), date(13), date(14))
    val summary = summaryOf(w1.map { obs(it, true) }, w2.map { preConfigDay(it) })
    val result = WeekdayStatsSelector(summary, tag)
    assertFalse(result.hasSufficientData)
  }

  // ── Average ──────────────────────────────────────────────────────────────────

  @Test
  fun `when sufficient data then averageCompletionRate equals arithmetic mean of weekday rates`() {
    // Week1: all done (1.0). Week2: Mon, Wed, Fri, Sun done; Tue, Thu, Sat not done.
    val summary = twoWeeks(
      List(7) { true },
      listOf(true, false, true, false, true, false, true),
    )
    // Mon=1.0, Tue=0.5, Wed=1.0, Thu=0.5, Fri=1.0, Sat=0.5, Sun=1.0
    val expectedAvg = (1.0 + 0.5 + 1.0 + 0.5 + 1.0 + 0.5 + 1.0) / 7.0
    val result = WeekdayStatsSelector(summary, tag)
    assertTrue(result.hasSufficientData)
    val avg = checkNotNull(result.averageCompletionRate) { "averageCompletionRate must not be null" }
    assertTrue(kotlin.math.abs(avg.toDouble() - expectedAvg) < 0.001)
  }

  // ── Helpers ──────────────────────────────────────────────────────────────────

  // Use a fixed Mon 2024-01-01 as anchor. DayOfWeek.entries is Mon-first.
  // date(1) = Mon 2024-01-01, date(2) = Tue 2024-01-02, etc.
  private fun date(dayOfMonth: Int) = LocalDate(2024, 1, dayOfMonth)

  private fun obs(date: LocalDate, done: Boolean): ContributionDay = ContributionDay(
    date = date,
    count = if (done) 1 else 0,
    totalHabits = 1,
    completionRatio = if (done) 1f else 0f,
    habitStatuses = listOf(HabitStatus(tag = tag, label = "Test", done = done)),
    dailyMemo = null,
    inRange = true,
    isClickable = true,
  )

  private fun outOfRange(date: LocalDate): ContributionDay = ContributionDay(
    date = date,
    count = 1,
    totalHabits = 1,
    completionRatio = 1f,
    habitStatuses = listOf(HabitStatus(tag = tag, label = "Test", done = true)),
    dailyMemo = null,
    inRange = false,
    isClickable = false,
  )

  private fun futureDay(date: LocalDate): ContributionDay = ContributionDay(
    date = date,
    count = 0,
    totalHabits = 1,
    completionRatio = 0f,
    habitStatuses = emptyList(),
    dailyMemo = null,
    inRange = true,
    isClickable = false,
  )

  private fun preConfigDay(date: LocalDate): ContributionDay = ContributionDay(
    date = date,
    count = 0,
    totalHabits = 0,
    completionRatio = 0f,
    habitStatuses = emptyList(), // habit not in config yet
    dailyMemo = null,
    inRange = true,
    isClickable = true,
  )

  private fun summaryOf(vararg weeks: List<ContributionDay>): ActivitySummary =
    ActivitySummary(
      weeks = weeks.mapIndexed { i, days ->
        ActivityWeek(
          startDate = days.first().date,
          days = days,
          weeklyCount = days.count { it.count > 0 },
        )
      },
      maxDaily = 1,
      maxWeekly = 7,
    )

  private fun twoWeeks(week1Done: List<Boolean>, week2Done: List<Boolean>): ActivitySummary {
    // Week 1: Mon 2024-01-01 to Sun 2024-01-07
    // Week 2: Mon 2024-01-08 to Sun 2024-01-14
    val w1 = week1Done.mapIndexed { i, done -> obs(date(1 + i), done) }
    val w2 = week2Done.mapIndexed { i, done -> obs(date(8 + i), done) }
    return summaryOf(w1, w2)
  }
}
