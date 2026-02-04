package space.be1ski.vibits.feature.habits.domain.usecase

import kotlinx.coroutines.test.runTest
import space.be1ski.vibits.core.platform.mode.AppMode
import space.be1ski.vibits.feature.habits.domain.model.ActivityMode
import space.be1ski.vibits.feature.memos.domain.model.Memo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

class PrewarmActivityDataUseCaseTest {
  private val buildActivityDataUseCase = BuildActivityDataUseCase(buildDayDataUseCase = BuildDayDataUseCase())
  private val calculateActivityDataUseCase =
    CalculateActivityDataUseCase(
      buildActivityDataUseCase,
    )
  private val useCase = PrewarmActivityDataUseCase(calculateActivityDataUseCase)

  @Test
  fun `when memos is empty then returns empty list`() =
    runTest {
      val result = useCase(emptyList(), AppMode.ONLINE)

      assertTrue(result.isEmpty())
    }

  @Test
  fun `when memos have no extractable dates then returns empty list`() =
    runTest {
      val memos =
        listOf(
          Memo(
            name = "memos/test",
            content = "No date content",
            createTime = null,
            updateTime = null,
          ),
        )

      val result = useCase(memos, AppMode.ONLINE)

      assertTrue(result.isEmpty())
    }

  @Test
  fun `when memos exist then generates results for both modes`() =
    runTest {
      val memos =
        listOf(
          createMemo(
            content = "Regular memo",
            createTime = Instant.parse("2024-01-15T10:00:00Z"),
          ),
        )

      val result = useCase(memos, AppMode.ONLINE)

      val modes = result.map { it.mode }.distinct()
      assertEquals(2, modes.size)
      assertTrue(ActivityMode.HABITS in modes)
      assertTrue(ActivityMode.POSTS in modes)
    }

  @Test
  fun `when memos exist then all results have correct appMode`() =
    runTest {
      val memos =
        listOf(
          createMemo(
            content = "Regular memo",
            createTime = Instant.parse("2024-01-15T10:00:00Z"),
          ),
        )

      val result = useCase(memos, AppMode.DEMO)

      assertTrue(result.isNotEmpty())
      assertTrue(result.all { it.appMode == AppMode.DEMO })
    }

  @Test
  fun `when memos exist then results contain weekData for each range and mode`() =
    runTest {
      val memos =
        listOf(
          createMemo(
            content = "Regular memo",
            createTime = Instant.parse("2024-01-15T10:00:00Z"),
          ),
        )

      val result = useCase(memos, AppMode.ONLINE)

      assertTrue(result.isNotEmpty())
      assertTrue(result.all { it.weekData.weeks.isNotEmpty() })
    }

  @Test
  fun `when multiple memos exist then ranges cover from earliest to today`() =
    runTest {
      val memos =
        listOf(
          createMemo(
            content = "Older memo",
            createTime = Instant.parse("2024-01-01T10:00:00Z"),
          ),
          createMemo(
            content = "Recent memo",
            createTime = Instant.parse("2024-06-15T10:00:00Z"),
          ),
        )

      val result = useCase(memos, AppMode.ONLINE)

      assertTrue(result.isNotEmpty())
      val ranges = result.map { it.range }.distinct()
      assertTrue(ranges.isNotEmpty())
    }

  @Test
  fun `when config memo exists then HABITS mode results have configTimeline`() =
    runTest {
      val memos =
        listOf(
          createMemo(
            content = "#habits/config\n- reading\n- exercise",
            createTime = Instant.parse("2024-01-10T10:00:00Z"),
          ),
          createMemo(
            content = "#habits/daily 2024-01-15\n- [x] reading",
            createTime = Instant.parse("2024-01-15T10:00:00Z"),
          ),
        )

      val result = useCase(memos, AppMode.ONLINE)

      val habitsResults = result.filter { it.mode == ActivityMode.HABITS }
      assertTrue(habitsResults.isNotEmpty())
      assertTrue(habitsResults.all { it.configTimeline.isNotEmpty() })
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
