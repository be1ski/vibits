package space.be1ski.vibits.shared.feature.habits.domain.usecase

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import space.be1ski.vibits.shared.feature.habits.domain.model.RangeBounds
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant as KtInstant

class CountDailyPostsUseCaseTest {
  private val timeZone = TimeZone.UTC

  @Test
  fun `when posts are within bounds then counts them per day`() {
    val memos =
      listOf(
        createMemo(KtInstant.parse("2024-01-15T10:00:00Z")),
        createMemo(KtInstant.parse("2024-01-15T15:00:00Z")),
        createMemo(KtInstant.parse("2024-01-16T10:00:00Z")),
      )
    val bounds =
      RangeBounds(
        start = LocalDate(2024, 1, 15),
        end = LocalDate(2024, 1, 16),
      )

    val result = CountDailyPostsUseCase(memos, timeZone, bounds)

    assertEquals(2, result[LocalDate(2024, 1, 15)])
    assertEquals(1, result[LocalDate(2024, 1, 16)])
  }

  @Test
  fun `when post is before start date then excludes it`() {
    val memos =
      listOf(
        createMemo(KtInstant.parse("2024-01-14T10:00:00Z")),
        createMemo(KtInstant.parse("2024-01-15T10:00:00Z")),
      )
    val bounds =
      RangeBounds(
        start = LocalDate(2024, 1, 15),
        end = LocalDate(2024, 1, 20),
      )

    val result = CountDailyPostsUseCase(memos, timeZone, bounds)

    assertEquals(null, result[LocalDate(2024, 1, 14)])
    assertEquals(1, result[LocalDate(2024, 1, 15)])
  }

  @Test
  fun `when post is after end date then excludes it`() {
    val memos =
      listOf(
        createMemo(KtInstant.parse("2024-01-15T10:00:00Z")),
        createMemo(KtInstant.parse("2024-01-21T10:00:00Z")),
      )
    val bounds =
      RangeBounds(
        start = LocalDate(2024, 1, 15),
        end = LocalDate(2024, 1, 20),
      )

    val result = CountDailyPostsUseCase(memos, timeZone, bounds)

    assertEquals(1, result[LocalDate(2024, 1, 15)])
    assertEquals(null, result[LocalDate(2024, 1, 21)])
  }

  @Test
  fun `when memos list is empty then returns empty map`() {
    val bounds =
      RangeBounds(
        start = LocalDate(2024, 1, 15),
        end = LocalDate(2024, 1, 20),
      )

    val result = CountDailyPostsUseCase(emptyList(), timeZone, bounds)

    assertEquals(0, result.size)
  }

  @Test
  fun `when posts are on boundary dates then includes them`() {
    val memos =
      listOf(
        createMemo(KtInstant.parse("2024-01-15T00:00:00Z")),
        createMemo(KtInstant.parse("2024-01-20T23:59:59Z")),
      )
    val bounds =
      RangeBounds(
        start = LocalDate(2024, 1, 15),
        end = LocalDate(2024, 1, 20),
      )

    val result = CountDailyPostsUseCase(memos, timeZone, bounds)

    assertEquals(1, result[LocalDate(2024, 1, 15)])
    assertEquals(1, result[LocalDate(2024, 1, 20)])
  }

  private fun createMemo(createTime: KtInstant): Memo =
    Memo(
      name = "memos/test",
      content = "Test content",
      createTime = createTime,
      updateTime = null,
    )
}
