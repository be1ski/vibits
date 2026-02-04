package space.be1ski.vibits.feature.habits.domain.usecase

import space.be1ski.vibits.feature.memos.domain.model.Memo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class FilterPostsUseCaseTest {
  private val testInstant = Instant.parse("2024-01-15T10:00:00Z")

  @Test
  fun `when memos contain habits hashtag then filters them out`() {
    val habitsConfig = createMemo("#habits/config\n- habit1")
    val habitsDaily = createMemo("#habits/daily 2024-01-15")
    val regularPost = createMemo("Just a regular note")

    val result = FilterPostsUseCase(listOf(habitsConfig, habitsDaily, regularPost))

    assertEquals(1, result.size)
    assertEquals("Just a regular note", result[0].content)
  }

  @Test
  fun `when all memos are habits then returns empty list`() {
    val habitsConfig = createMemo("#habits/config")
    val habitsDaily = createMemo("#habits/daily")

    val result = FilterPostsUseCase(listOf(habitsConfig, habitsDaily))

    assertEquals(0, result.size)
  }

  @Test
  fun `when no memos contain habits then returns all`() {
    val post1 = createMemo("First regular post")
    val post2 = createMemo("Second regular post #other")

    val result = FilterPostsUseCase(listOf(post1, post2))

    assertEquals(2, result.size)
  }

  @Test
  fun `when memos list is empty then returns empty list`() {
    val result = FilterPostsUseCase(emptyList())

    assertEquals(0, result.size)
  }

  @Test
  fun `when habits hashtag appears anywhere in content then filters out`() {
    val memoWithHashtagInMiddle = createMemo("Some text #habits in the middle")
    val regularPost = createMemo("Regular post")

    val result = FilterPostsUseCase(listOf(memoWithHashtagInMiddle, regularPost))

    assertEquals(1, result.size)
    assertEquals("Regular post", result[0].content)
  }

  private fun createMemo(content: String): Memo =
    Memo(
      name = "memos/test",
      content = content,
      createTime = testInstant,
    )
}
