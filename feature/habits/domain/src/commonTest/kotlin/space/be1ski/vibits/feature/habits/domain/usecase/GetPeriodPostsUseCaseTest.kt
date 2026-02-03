package space.be1ski.vibits.feature.habits.domain.usecase

import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import space.be1ski.vibits.feature.habits.domain.model.ActivityRange
import space.be1ski.vibits.feature.memos.domain.model.Memo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

class GetPeriodPostsUseCaseTest {
  private val timeZone = TimeZone.UTC

  private fun createMemo(
    content: String,
    createTime: Instant,
  ) = Memo(
    name = "memos/$content",
    content = content,
    createTime = createTime,
    updateTime = createTime,
  )

  @Test
  fun `when range is Week then returns posts in that week`() {
    val startDate = LocalDate(2024, 1, 1) // Monday
    val memos =
      listOf(
        createMemo("Post 1", Instant.parse("2024-01-01T10:00:00Z")), // Monday
        createMemo("Post 2", Instant.parse("2024-01-03T10:00:00Z")), // Wednesday
        createMemo("Post 3", Instant.parse("2024-01-07T10:00:00Z")), // Sunday (last day)
        createMemo("Post 4", Instant.parse("2024-01-08T10:00:00Z")), // Next Monday (outside)
      )
    val useCase = GetPeriodPostsUseCase()

    val result = useCase(memos, ActivityRange.Week(startDate), timeZone)

    assertEquals(3, result.size)
    assertEquals("Post 3", result[0].content) // Sorted descending
    assertEquals("Post 2", result[1].content)
    assertEquals("Post 1", result[2].content)
  }

  @Test
  fun `when range is Month then returns posts in that month`() {
    val memos =
      listOf(
        createMemo("Post 1", Instant.parse("2024-01-01T10:00:00Z")), // First day
        createMemo("Post 2", Instant.parse("2024-01-15T10:00:00Z")), // Mid month
        createMemo("Post 3", Instant.parse("2024-01-31T10:00:00Z")), // Last day
        createMemo("Post 4", Instant.parse("2024-02-01T10:00:00Z")), // Next month (outside)
      )
    val useCase = GetPeriodPostsUseCase()

    val result = useCase(memos, ActivityRange.Month(year = 2024, month = Month.JANUARY), timeZone)

    assertEquals(3, result.size)
    assertEquals("Post 3", result[0].content)
    assertEquals("Post 2", result[1].content)
    assertEquals("Post 1", result[2].content)
  }

  @Test
  fun `when range is Quarter then returns posts in that quarter`() {
    val memos =
      listOf(
        createMemo("Post 1", Instant.parse("2024-01-01T10:00:00Z")), // Q1 start
        createMemo("Post 2", Instant.parse("2024-02-15T10:00:00Z")), // Q1 middle
        createMemo("Post 3", Instant.parse("2024-03-31T10:00:00Z")), // Q1 end
        createMemo("Post 4", Instant.parse("2024-04-01T10:00:00Z")), // Q2 start (outside)
      )
    val useCase = GetPeriodPostsUseCase()

    val result = useCase(memos, ActivityRange.Quarter(year = 2024, index = 1), timeZone)

    assertEquals(3, result.size)
    assertEquals("Post 3", result[0].content)
    assertEquals("Post 2", result[1].content)
    assertEquals("Post 1", result[2].content)
  }

  @Test
  fun `when range is Quarter 2 then calculates correct months`() {
    val memos =
      listOf(
        createMemo("Post 1", Instant.parse("2024-04-01T10:00:00Z")), // Q2 start (Apr)
        createMemo("Post 2", Instant.parse("2024-05-15T10:00:00Z")), // Q2 middle (May)
        createMemo("Post 3", Instant.parse("2024-06-30T10:00:00Z")), // Q2 end (Jun)
        createMemo("Post 4", Instant.parse("2024-07-01T10:00:00Z")), // Q3 start (outside)
      )
    val useCase = GetPeriodPostsUseCase()

    val result = useCase(memos, ActivityRange.Quarter(year = 2024, index = 2), timeZone)

    assertEquals(3, result.size)
  }

  @Test
  fun `when range is Year then returns posts in that year`() {
    val memos =
      listOf(
        createMemo("Post 1", Instant.parse("2024-01-01T10:00:00Z")), // First day
        createMemo("Post 2", Instant.parse("2024-06-15T10:00:00Z")), // Mid year
        createMemo("Post 3", Instant.parse("2024-12-31T10:00:00Z")), // Last day
        createMemo("Post 4", Instant.parse("2025-01-01T10:00:00Z")), // Next year (outside)
      )
    val useCase = GetPeriodPostsUseCase()

    val result = useCase(memos, ActivityRange.Year(year = 2024), timeZone)

    assertEquals(3, result.size)
    assertEquals("Post 3", result[0].content)
    assertEquals("Post 2", result[1].content)
    assertEquals("Post 1", result[2].content)
  }

  @Test
  fun `when memo has habits hashtag then excludes it from posts`() {
    val memos =
      listOf(
        createMemo("Regular post", Instant.parse("2024-01-01T10:00:00Z")),
        Memo(
          name = "memos/habit",
          content = "#habits/exercise Did workout",
          createTime = Instant.parse("2024-01-02T10:00:00Z"),
          updateTime = Instant.parse("2024-01-02T10:00:00Z"),
        ),
      )
    val useCase = GetPeriodPostsUseCase()

    val result = useCase(memos, ActivityRange.Week(LocalDate(2024, 1, 1)), timeZone)

    assertEquals(1, result.size)
    assertEquals("Regular post", result[0].content)
  }

  @Test
  fun `when memo has no createTime or updateTime then excludes it`() {
    val memos =
      listOf(
        createMemo("Post 1", Instant.parse("2024-01-01T10:00:00Z")),
        Memo(
          name = "memos/no-time",
          content = "No timestamps",
          createTime = null,
          updateTime = null,
        ),
      )
    val useCase = GetPeriodPostsUseCase()

    val result = useCase(memos, ActivityRange.Week(LocalDate(2024, 1, 1)), timeZone)

    assertEquals(1, result.size)
    assertEquals("Post 1", result[0].content)
  }

  @Test
  fun `when memo has updateTime but no createTime then uses updateTime`() {
    val memos =
      listOf(
        Memo(
          name = "memos/updated",
          content = "Updated post",
          createTime = null,
          updateTime = Instant.parse("2024-01-01T10:00:00Z"),
        ),
      )
    val useCase = GetPeriodPostsUseCase()

    val result = useCase(memos, ActivityRange.Week(LocalDate(2024, 1, 1)), timeZone)

    assertEquals(1, result.size)
    assertEquals("Updated post", result[0].content)
  }

  @Test
  fun `when no posts in range then returns empty list`() {
    val memos =
      listOf(
        createMemo("Post 1", Instant.parse("2023-12-31T10:00:00Z")), // Before range
        createMemo("Post 2", Instant.parse("2024-01-08T10:00:00Z")), // After range
      )
    val useCase = GetPeriodPostsUseCase()

    val result = useCase(memos, ActivityRange.Week(LocalDate(2024, 1, 1)), timeZone)

    assertTrue(result.isEmpty())
  }

  @Test
  fun `when empty memos list then returns empty list`() {
    val useCase = GetPeriodPostsUseCase()

    val result = useCase(emptyList(), ActivityRange.Week(LocalDate(2024, 1, 1)), timeZone)

    assertTrue(result.isEmpty())
  }

  @Test
  fun `when February leap year then calculates month correctly`() {
    val memos =
      listOf(
        createMemo("Post 1", Instant.parse("2024-02-29T10:00:00Z")), // Leap day
        createMemo("Post 2", Instant.parse("2024-03-01T10:00:00Z")), // Next month
      )
    val useCase = GetPeriodPostsUseCase()

    val result = useCase(memos, ActivityRange.Month(year = 2024, month = Month.FEBRUARY), timeZone)

    assertEquals(1, result.size)
    assertEquals("Post 1", result[0].content)
  }
}
