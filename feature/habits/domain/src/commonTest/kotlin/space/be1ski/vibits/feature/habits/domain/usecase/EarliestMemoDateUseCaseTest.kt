package space.be1ski.vibits.feature.habits.domain.usecase

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import space.be1ski.vibits.feature.memos.domain.model.Memo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant as KtInstant

class EarliestMemoDateUseCaseTest {
  private val timeZone = TimeZone.UTC

  @Test
  fun `when memos is empty then returns null`() {
    val result = EarliestMemoDateUseCase(emptyList(), timeZone)

    assertNull(result)
  }

  @Test
  fun `when single memo with date in content then returns that date`() {
    val memo =
      createMemo(
        content = "#habits/daily 2024-03-15\n- task",
        createTime = KtInstant.parse("2024-05-01T10:00:00Z"),
      )

    val result = EarliestMemoDateUseCase(listOf(memo), timeZone)

    assertEquals(LocalDate(2024, 3, 15), result)
  }

  @Test
  fun `when single memo without date in content then uses createTime`() {
    val memo =
      createMemo(
        content = "Regular memo content",
        createTime = KtInstant.parse("2024-06-20T15:30:00Z"),
      )

    val result = EarliestMemoDateUseCase(listOf(memo), timeZone)

    assertEquals(LocalDate(2024, 6, 20), result)
  }

  @Test
  fun `when multiple memos then returns earliest date from content`() {
    val memos =
      listOf(
        createMemo(
          content = "#daily 2024-02-10\n- task",
          createTime = KtInstant.parse("2024-05-01T10:00:00Z"),
        ),
        createMemo(
          content = "#habits/daily 2024-01-05\n- task",
          createTime = KtInstant.parse("2024-05-15T10:00:00Z"),
        ),
        createMemo(
          content = "#daily 2024-03-20\n- task",
          createTime = KtInstant.parse("2024-04-01T10:00:00Z"),
        ),
      )

    val result = EarliestMemoDateUseCase(memos, timeZone)

    assertEquals(LocalDate(2024, 1, 5), result)
  }

  @Test
  fun `when multiple memos then returns earliest date from timestamps`() {
    val memos =
      listOf(
        createMemo(
          content = "Regular memo 1",
          createTime = KtInstant.parse("2024-03-15T10:00:00Z"),
        ),
        createMemo(
          content = "Regular memo 2",
          createTime = KtInstant.parse("2024-01-10T10:00:00Z"),
        ),
        createMemo(
          content = "Regular memo 3",
          createTime = KtInstant.parse("2024-02-20T10:00:00Z"),
        ),
      )

    val result = EarliestMemoDateUseCase(memos, timeZone)

    assertEquals(LocalDate(2024, 1, 10), result)
  }

  @Test
  fun `when memos mix content dates and timestamps then returns overall earliest`() {
    val memos =
      listOf(
        createMemo(
          content = "#daily 2024-06-01\n- task",
          createTime = KtInstant.parse("2024-06-15T10:00:00Z"),
        ),
        createMemo(
          content = "Regular memo",
          createTime = KtInstant.parse("2024-03-10T10:00:00Z"),
        ),
      )

    val result = EarliestMemoDateUseCase(memos, timeZone)

    assertEquals(LocalDate(2024, 3, 10), result)
  }

  @Test
  fun `when memo has no date in content and no timestamp then skips it`() {
    val memoWithDate =
      createMemo(
        content = "#daily 2024-04-15\n- task",
        createTime = KtInstant.parse("2024-04-15T10:00:00Z"),
      )
    val memoWithoutTimestamp =
      Memo(
        name = "memos/orphan",
        content = "No timestamp memo",
        createTime = null,
        updateTime = null,
      )

    val result = EarliestMemoDateUseCase(listOf(memoWithDate, memoWithoutTimestamp), timeZone)

    assertEquals(LocalDate(2024, 4, 15), result)
  }

  @Test
  fun `when all memos have no extractable date then returns null`() {
    val memos =
      listOf(
        Memo(
          name = "memos/1",
          content = "No date content",
          createTime = null,
          updateTime = null,
        ),
        Memo(
          name = "memos/2",
          content = "Another memo without date",
          createTime = null,
          updateTime = null,
        ),
      )

    val result = EarliestMemoDateUseCase(memos, timeZone)

    assertNull(result)
  }

  @Test
  fun `when timezone affects date then respects timezone`() {
    val memo =
      createMemo(
        content = "Regular memo",
        createTime = KtInstant.parse("2024-01-15T23:00:00Z"),
      )
    val tokyoTimeZone = TimeZone.of("Asia/Tokyo")

    val result = EarliestMemoDateUseCase(listOf(memo), tokyoTimeZone)

    assertEquals(LocalDate(2024, 1, 16), result)
  }

  private fun createMemo(
    content: String,
    createTime: KtInstant,
    updateTime: KtInstant? = null,
  ): Memo =
    Memo(
      name = "memos/test",
      content = content,
      createTime = createTime,
      updateTime = updateTime,
    )
}
