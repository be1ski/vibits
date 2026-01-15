package space.be1ski.vibits.shared.feature.habits.domain.usecase

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import space.be1ski.vibits.shared.feature.memos.domain.model.Memo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Instant as KtInstant

class ExtractDailyMemosUseCaseTest {

  private val useCase = ExtractDailyMemosUseCase()
  private val timeZone = TimeZone.UTC

  @Test
  fun `extracts daily memos with habits daily tag`() {
    val memo = createMemo(
      content = "#habits/daily 2024-01-15\n- completed task",
      createTime = KtInstant.parse("2024-01-15T10:00:00Z")
    )

    val result = useCase(listOf(memo), timeZone)

    assertEquals(1, result.size)
    assertNotNull(result[LocalDate(2024, 1, 15)])
  }

  @Test
  fun `extracts daily memos with daily tag`() {
    val memo = createMemo(
      content = "#daily 2024-01-15\n- completed task",
      createTime = KtInstant.parse("2024-01-15T10:00:00Z")
    )

    val result = useCase(listOf(memo), timeZone)

    assertEquals(1, result.size)
    assertNotNull(result[LocalDate(2024, 1, 15)])
  }

  @Test
  fun `ignores non-daily memos`() {
    val memo = createMemo(
      content = "Regular memo without daily tag",
      createTime = KtInstant.parse("2024-01-15T10:00:00Z")
    )

    val result = useCase(listOf(memo), timeZone)

    assertEquals(0, result.size)
  }

  @Test
  fun `parses date from content when available`() {
    val memo = createMemo(
      content = "#daily 2024-02-20\n- task",
      createTime = KtInstant.parse("2024-01-15T10:00:00Z")
    )

    val result = useCase(listOf(memo), timeZone)

    assertNotNull(result[LocalDate(2024, 2, 20)])
    assertNull(result[LocalDate(2024, 1, 15)])
  }

  @Test
  fun `forDate returns memo for specific date`() {
    val memo = createMemo(
      content = "#daily 2024-01-15\n- task",
      createTime = KtInstant.parse("2024-01-15T10:00:00Z")
    )

    val result = useCase.forDate(listOf(memo), timeZone, LocalDate(2024, 1, 15))

    assertNotNull(result)
    assertEquals("#daily 2024-01-15\n- task", result.content)
  }

  @Test
  fun `forDate returns null for non-existing date`() {
    val memo = createMemo(
      content = "#daily 2024-01-15\n- task",
      createTime = KtInstant.parse("2024-01-15T10:00:00Z")
    )

    val result = useCase.forDate(listOf(memo), timeZone, LocalDate(2024, 1, 20))

    assertNull(result)
  }

  @Test
  fun `parseDailyDateFromContent extracts date correctly`() {
    val content = "#habits/daily 2024-03-25\n- some task"

    val result = ExtractDailyMemosUseCase.parseDailyDateFromContent(content)

    assertEquals(LocalDate(2024, 3, 25), result)
  }

  @Test
  fun `parseDailyDateFromContent returns null for content without date`() {
    val content = "#daily\n- some task"

    val result = ExtractDailyMemosUseCase.parseDailyDateFromContent(content)

    assertNull(result)
  }

  @Test
  fun `parseDailyDateFromContent returns null for non-daily content`() {
    val content = "Regular content 2024-01-15"

    val result = ExtractDailyMemosUseCase.parseDailyDateFromContent(content)

    assertNull(result)
  }

  @Test
  fun `parseMemoDate extracts date from memo timestamp`() {
    val memo = createMemo(
      content = "any content",
      createTime = KtInstant.parse("2024-05-10T15:30:00Z")
    )

    val result = ExtractDailyMemosUseCase.parseMemoDate(memo, timeZone)

    assertEquals(LocalDate(2024, 5, 10), result)
  }

  @Test
  fun `parseMemoInstant prefers updateTime over createTime`() {
    val memo = createMemo(
      content = "any content",
      createTime = KtInstant.parse("2024-01-10T10:00:00Z"),
      updateTime = KtInstant.parse("2024-01-15T15:00:00Z")
    )

    val result = ExtractDailyMemosUseCase.parseMemoInstant(memo)

    assertEquals(KtInstant.parse("2024-01-15T15:00:00Z"), result)
  }

  private fun createMemo(
    content: String,
    createTime: KtInstant,
    updateTime: KtInstant? = null
  ): Memo {
    return Memo(
      name = "memos/test",
      content = content,
      createTime = createTime,
      updateTime = updateTime
    )
  }
}
