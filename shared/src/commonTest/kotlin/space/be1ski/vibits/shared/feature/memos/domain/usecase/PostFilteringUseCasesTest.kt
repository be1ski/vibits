package space.be1ski.vibits.shared.feature.memos.domain.usecase

import space.be1ski.vibits.shared.feature.memos.domain.model.Memo
import space.be1ski.vibits.shared.feature.memos.domain.model.PostFilter
import space.be1ski.vibits.shared.feature.memos.domain.model.PostTags
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class PostFilteringUseCasesTest {
  private val testInstant = Instant.parse("2024-01-01T00:00:00Z")

  // ClassifyPostTypeUseCase tests
  @Test
  fun `when memo contains habits config tag then classifies as CONFIG`() {
    val memo =
      Memo(
        name = "test",
        content = "Some content ${PostTags.HABITS_CONFIG} more text",
        createTime = testInstant,
      )

    val result = ClassifyPostTypeUseCase(memo)

    assertEquals(PostFilter.CONFIG, result)
  }

  @Test
  fun `when memo contains habits_config tag then classifies as CONFIG`() {
    val memo =
      Memo(
        name = "test",
        content = "Content with ${PostTags.HABITS_CONFIG_ALT} tag",
        createTime = testInstant,
      )

    val result = ClassifyPostTypeUseCase(memo)

    assertEquals(PostFilter.CONFIG, result)
  }

  @Test
  fun `when memo contains habits daily tag then classifies as HABIT_TRACKING`() {
    val memo =
      Memo(
        name = "test",
        content = "Daily log ${PostTags.HABITS_DAILY} completed",
        createTime = testInstant,
      )

    val result = ClassifyPostTypeUseCase(memo)

    assertEquals(PostFilter.HABIT_TRACKING, result)
  }

  @Test
  fun `when memo contains daily tag then classifies as HABIT_TRACKING`() {
    val memo =
      Memo(
        name = "test",
        content = "Today's habits ${PostTags.DAILY}",
        createTime = testInstant,
      )

    val result = ClassifyPostTypeUseCase(memo)

    assertEquals(PostFilter.HABIT_TRACKING, result)
  }

  @Test
  fun `when memo has no habit tags then classifies as REGULAR`() {
    val memo =
      Memo(
        name = "test",
        content = "Just a regular note #random #tags",
        createTime = testInstant,
      )

    val result = ClassifyPostTypeUseCase(memo)

    assertEquals(PostFilter.REGULAR, result)
  }

  @Test
  fun `when memo is empty then classifies as REGULAR`() {
    val memo =
      Memo(
        name = "test",
        content = "",
        createTime = testInstant,
      )

    val result = ClassifyPostTypeUseCase(memo)

    assertEquals(PostFilter.REGULAR, result)
  }

  @Test
  fun `when classification is case insensitive then works correctly`() {
    val memo1 = Memo(name = "test1", content = "#HABITS/CONFIG", createTime = testInstant)
    val memo2 = Memo(name = "test2", content = "#Habits/Daily", createTime = testInstant)

    assertEquals(PostFilter.CONFIG, ClassifyPostTypeUseCase(memo1))
    assertEquals(PostFilter.HABIT_TRACKING, ClassifyPostTypeUseCase(memo2))
  }

  @Test
  fun `when memo has both config and tracking tags then config takes priority`() {
    val memo =
      Memo(
        name = "test",
        content = "${PostTags.HABITS_CONFIG} and ${PostTags.HABITS_DAILY}",
        createTime = testInstant,
      )

    val result = ClassifyPostTypeUseCase(memo)

    assertEquals(PostFilter.CONFIG, result)
  }

  // FilterMemosByTypeUseCase tests
  @Test
  fun `when filter is ALL then returns all memos`() {
    val memos =
      listOf(
        Memo(name = "1", content = PostTags.HABITS_CONFIG, createTime = testInstant),
        Memo(name = "2", content = PostTags.HABITS_DAILY, createTime = testInstant),
        Memo(name = "3", content = "regular note", createTime = testInstant),
      )

    val result = FilterMemosByTypeUseCase(memos, PostFilter.ALL)

    assertEquals(3, result.size)
    assertEquals(memos, result)
  }

  @Test
  fun `when filter is CONFIG then returns only config memos`() {
    val configMemo1 = Memo(name = "1", content = PostTags.HABITS_CONFIG, createTime = testInstant)
    val configMemo2 = Memo(name = "2", content = PostTags.HABITS_CONFIG_ALT, createTime = testInstant)
    val trackingMemo = Memo(name = "3", content = PostTags.HABITS_DAILY, createTime = testInstant)
    val regularMemo = Memo(name = "4", content = "note", createTime = testInstant)

    val memos = listOf(configMemo1, trackingMemo, configMemo2, regularMemo)

    val result = FilterMemosByTypeUseCase(memos, PostFilter.CONFIG)

    assertEquals(2, result.size)
    assertEquals(listOf(configMemo1, configMemo2), result)
  }

  @Test
  fun `when filter is HABIT_TRACKING then returns only tracking memos`() {
    val trackingMemo1 = Memo(name = "1", content = PostTags.HABITS_DAILY, createTime = testInstant)
    val trackingMemo2 = Memo(name = "2", content = PostTags.DAILY, createTime = testInstant)
    val configMemo = Memo(name = "3", content = PostTags.HABITS_CONFIG, createTime = testInstant)
    val regularMemo = Memo(name = "4", content = "note", createTime = testInstant)

    val memos = listOf(trackingMemo1, configMemo, trackingMemo2, regularMemo)

    val result = FilterMemosByTypeUseCase(memos, PostFilter.HABIT_TRACKING)

    assertEquals(2, result.size)
    assertEquals(listOf(trackingMemo1, trackingMemo2), result)
  }

  @Test
  fun `when filter is REGULAR then returns only regular memos`() {
    val regularMemo1 = Memo(name = "1", content = "note 1", createTime = testInstant)
    val regularMemo2 = Memo(name = "2", content = "note 2 #other", createTime = testInstant)
    val configMemo = Memo(name = "3", content = PostTags.HABITS_CONFIG, createTime = testInstant)
    val trackingMemo = Memo(name = "4", content = PostTags.DAILY, createTime = testInstant)

    val memos = listOf(regularMemo1, configMemo, regularMemo2, trackingMemo)

    val result = FilterMemosByTypeUseCase(memos, PostFilter.REGULAR)

    assertEquals(2, result.size)
    assertEquals(listOf(regularMemo1, regularMemo2), result)
  }

  @Test
  fun `when memos list is empty then returns empty list`() {
    val result = FilterMemosByTypeUseCase(emptyList(), PostFilter.CONFIG)

    assertEquals(0, result.size)
  }

  @Test
  fun `when no memos match filter then returns empty list`() {
    val memos =
      listOf(
        Memo(name = "1", content = PostTags.HABITS_CONFIG, createTime = testInstant),
        Memo(name = "2", content = PostTags.HABITS_DAILY, createTime = testInstant),
      )

    val result = FilterMemosByTypeUseCase(memos, PostFilter.REGULAR)

    assertEquals(0, result.size)
  }
}
