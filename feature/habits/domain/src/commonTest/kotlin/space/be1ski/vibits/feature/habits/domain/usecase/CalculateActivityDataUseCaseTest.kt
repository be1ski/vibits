package space.be1ski.vibits.feature.habits.domain.usecase

import kotlinx.datetime.LocalDate
import space.be1ski.vibits.feature.habits.domain.model.ActivityMode
import space.be1ski.vibits.feature.habits.domain.model.ActivityRange
import space.be1ski.vibits.feature.memos.domain.model.Memo
import space.be1ski.vibits.feature.memos.domain.model.PostTags
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class CalculateActivityDataUseCaseTest {
  private val calculateSuccessRateUseCase = CalculateSuccessRateUseCase()
  private val useCase = CalculateActivityDataUseCase(calculateSuccessRateUseCase)

  @Test
  fun `when memos is empty then returns empty week data`() {
    val range = ActivityRange.Week(startDate = LocalDate(2024, 1, 15))

    val result = useCase(range, ActivityMode.POSTS, emptyList())

    assertTrue(result.weekData.weeks.isNotEmpty())
    assertTrue(result.configTimeline.isEmpty())
    assertNull(result.successRate)
  }

  @Test
  fun `when mode is POSTS then successRate is null`() {
    val range = ActivityRange.Week(startDate = LocalDate(2024, 1, 15))
    val memos =
      listOf(
        createMemo(
          content = "Regular post",
          createTime = Instant.parse("2024-01-16T10:00:00Z"),
        ),
      )

    val result = useCase(range, ActivityMode.POSTS, memos)

    assertNull(result.successRate)
    assertTrue(result.configTimeline.isEmpty())
  }

  @Test
  fun `when mode is HABITS and no config then successRate is null`() {
    val range = ActivityRange.Week(startDate = LocalDate(2024, 1, 15))
    val memos =
      listOf(
        createMemo(
          content = "#habits/daily 2024-01-16\n- [x] exercise",
          createTime = Instant.parse("2024-01-16T10:00:00Z"),
        ),
      )

    val result = useCase(range, ActivityMode.HABITS, memos)

    assertNull(result.successRate)
    assertTrue(result.configTimeline.isEmpty())
  }

  @Test
  fun `when mode is HABITS and config exists then successRate is calculated`() {
    val range = ActivityRange.Week(startDate = LocalDate(2024, 1, 15))
    val memos =
      listOf(
        createMemo(
          content = "#habits/config\n- exercise",
          createTime = Instant.parse("2024-01-10T10:00:00Z"),
        ),
        createMemo(
          content = "#habits/daily 2024-01-16\n- [x] exercise",
          createTime = Instant.parse("2024-01-16T10:00:00Z"),
        ),
      )

    val result = useCase(range, ActivityMode.HABITS, memos)

    assertNotNull(result.successRate)
    assertEquals(1, result.configTimeline.size)
  }

  @Test
  fun `when mode is HABITS then configTimeline is passed to buildWeekData`() {
    val range = ActivityRange.Week(startDate = LocalDate(2024, 1, 15))
    val memos =
      listOf(
        createMemo(
          content = "#habits/config\n- reading\n- exercise",
          createTime = Instant.parse("2024-01-10T10:00:00Z"),
        ),
        createMemo(
          content = "#habits/daily 2024-01-16\n- [x] reading\n- [ ] exercise",
          createTime = Instant.parse("2024-01-16T10:00:00Z"),
        ),
      )

    val result = useCase(range, ActivityMode.HABITS, memos)

    assertEquals(1, result.configTimeline.size)
    assertEquals(
      2,
      result.configTimeline
        .first()
        .habits.size,
    )
  }

  @Test
  fun `when mode is POSTS then configTimeline is empty in weekData calculation`() {
    val range = ActivityRange.Week(startDate = LocalDate(2024, 1, 15))
    val memos =
      listOf(
        createMemo(
          content = "#habits/config\n- reading",
          createTime = Instant.parse("2024-01-10T10:00:00Z"),
        ),
        createMemo(
          content = "Regular post",
          createTime = Instant.parse("2024-01-16T10:00:00Z"),
        ),
      )

    val result = useCase(range, ActivityMode.POSTS, memos)

    assertTrue(result.configTimeline.isNotEmpty())
    assertNull(result.successRate)
  }

  @Test
  fun `when multiple config entries then all are extracted in timeline`() {
    val range = ActivityRange.Week(startDate = LocalDate(2024, 1, 15))
    val memos =
      listOf(
        createMemo(
          content = "#habits/config\n- reading",
          createTime = Instant.parse("2024-01-01T10:00:00Z"),
        ),
        createMemo(
          content = "#habits/config\n- reading\n- exercise",
          createTime = Instant.parse("2024-01-10T10:00:00Z"),
        ),
      )

    val result = useCase(range, ActivityMode.HABITS, memos)

    assertEquals(2, result.configTimeline.size)
    assertEquals(1, result.configTimeline[0].habits.size)
    assertEquals(2, result.configTimeline[1].habits.size)
  }

  @Test
  fun `when weekData is built then maxDaily is calculated`() {
    val range = ActivityRange.Week(startDate = LocalDate(2024, 1, 15))
    val memos =
      listOf(
        createMemo(content = "post 1", createTime = Instant.parse("2024-01-16T10:00:00Z")),
        createMemo(content = "post 2", createTime = Instant.parse("2024-01-16T11:00:00Z")),
        createMemo(content = "post 3", createTime = Instant.parse("2024-01-16T12:00:00Z")),
        createMemo(content = "post 4", createTime = Instant.parse("2024-01-17T10:00:00Z")),
      )

    val result = useCase(range, ActivityMode.POSTS, memos)

    assertEquals(3, result.weekData.maxDaily)
  }

  private fun createMemo(
    content: String,
    createTime: Instant,
    name: String = "memos/test-${content.hashCode()}",
  ): Memo =
    Memo(
      name = name,
      content = content,
      createTime = createTime,
      updateTime = null,
    )
}
