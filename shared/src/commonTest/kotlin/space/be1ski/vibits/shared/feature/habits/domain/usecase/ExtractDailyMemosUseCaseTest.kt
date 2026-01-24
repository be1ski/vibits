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
  private val timeZone = TimeZone.UTC

  @Test
  fun `when memo has habits daily tag then extracts it`() {
    val memo =
      createMemo(
        content = "#habits/daily 2024-01-15\n- completed task",
        createTime = KtInstant.parse("2024-01-15T10:00:00Z"),
      )

    val result = ExtractDailyMemosUseCase(listOf(memo), timeZone)

    assertEquals(1, result.size)
    assertNotNull(result[LocalDate(2024, 1, 15)])
  }

  @Test
  fun `when memo has daily tag then extracts it`() {
    val memo =
      createMemo(
        content = "#daily 2024-01-15\n- completed task",
        createTime = KtInstant.parse("2024-01-15T10:00:00Z"),
      )

    val result = ExtractDailyMemosUseCase(listOf(memo), timeZone)

    assertEquals(1, result.size)
    assertNotNull(result[LocalDate(2024, 1, 15)])
  }

  @Test
  fun `when memo has no daily tag then ignores it`() {
    val memo =
      createMemo(
        content = "Regular memo without daily tag",
        createTime = KtInstant.parse("2024-01-15T10:00:00Z"),
      )

    val result = ExtractDailyMemosUseCase(listOf(memo), timeZone)

    assertEquals(0, result.size)
  }

  @Test
  fun `when content has date then uses content date instead of createTime`() {
    val memo =
      createMemo(
        content = "#daily 2024-02-20\n- task",
        createTime = KtInstant.parse("2024-01-15T10:00:00Z"),
      )

    val result = ExtractDailyMemosUseCase(listOf(memo), timeZone)

    assertNotNull(result[LocalDate(2024, 2, 20)])
    assertNull(result[LocalDate(2024, 1, 15)])
  }

  @Test
  fun `when forDate called with existing date then returns memo`() {
    val memo =
      createMemo(
        content = "#daily 2024-01-15\n- task",
        createTime = KtInstant.parse("2024-01-15T10:00:00Z"),
      )

    val result = ExtractDailyMemosUseCase.forDate(listOf(memo), timeZone, LocalDate(2024, 1, 15))

    assertNotNull(result)
    assertEquals("#daily 2024-01-15\n- task", result.content)
  }

  @Test
  fun `when forDate called with non-existing date then returns null`() {
    val memo =
      createMemo(
        content = "#daily 2024-01-15\n- task",
        createTime = KtInstant.parse("2024-01-15T10:00:00Z"),
      )

    val result = ExtractDailyMemosUseCase.forDate(listOf(memo), timeZone, LocalDate(2024, 1, 20))

    assertNull(result)
  }

  @Test
  fun `when content has daily tag with date then parseDailyDateFromContent extracts it`() {
    val content = "#habits/daily 2024-03-25\n- some task"

    val result = parseDailyDateFromContent(content)

    assertEquals(LocalDate(2024, 3, 25), result)
  }

  @Test
  fun `when content has daily tag without date then parseDailyDateFromContent returns null`() {
    val content = "#daily\n- some task"

    val result = parseDailyDateFromContent(content)

    assertNull(result)
  }

  @Test
  fun `when content has no daily tag then parseDailyDateFromContent returns null`() {
    val content = "Regular content 2024-01-15"

    val result = parseDailyDateFromContent(content)

    assertNull(result)
  }

  @Test
  fun `when memo has createTime then parseMemoDate extracts date`() {
    val memo =
      createMemo(
        content = "any content",
        createTime = KtInstant.parse("2024-05-10T15:30:00Z"),
      )

    val result = parseMemoDate(memo, timeZone)

    assertEquals(LocalDate(2024, 5, 10), result)
  }

  @Test
  fun `when memo has updateTime then parseMemoInstant prefers it over createTime`() {
    val memo =
      createMemo(
        content = "any content",
        createTime = KtInstant.parse("2024-01-10T10:00:00Z"),
        updateTime = KtInstant.parse("2024-01-15T15:00:00Z"),
      )

    val result = parseMemoInstant(memo)

    assertEquals(KtInstant.parse("2024-01-15T15:00:00Z"), result)
  }

  @Test
  fun `when daily memo has no date in content and no timestamp then skips it`() {
    val memo =
      Memo(
        name = "memos/test",
        content = "#daily\n- task",
        createTime = null,
        updateTime = null,
      )

    val result = ExtractDailyMemosUseCase(listOf(memo), timeZone)

    assertEquals(0, result.size)
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
