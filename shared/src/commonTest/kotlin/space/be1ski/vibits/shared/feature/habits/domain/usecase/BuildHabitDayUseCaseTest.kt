package space.be1ski.vibits.shared.feature.habits.domain.usecase

import kotlinx.datetime.LocalDate
import space.be1ski.vibits.shared.feature.habits.domain.model.DailyMemoInfo
import space.be1ski.vibits.shared.feature.habits.domain.model.HabitConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BuildHabitDayUseCaseTest {
  private val useCase = BuildHabitDayUseCase()

  @Test
  fun `returns null when habitsConfig is empty`() {
    val date = LocalDate(2024, 1, 15)
    val dailyMemo = DailyMemoInfo(name = "test", content = "content")

    val result = useCase(date, emptyList(), dailyMemo)

    assertNull(result)
  }

  @Test
  fun `builds day with completed habits`() {
    val date = LocalDate(2024, 1, 15)
    val habitsConfig =
      listOf(
        HabitConfig(tag = "#habits/exercise", label = "Exercise"),
        HabitConfig(tag = "#habits/reading", label = "Reading"),
      )
    val dailyMemo =
      DailyMemoInfo(
        name = "test",
        content = "#daily 2024-01-15\n#habits/exercise\n#habits/reading",
      )

    val result = useCase(date, habitsConfig, dailyMemo)

    assertNotNull(result)
    assertEquals(date, result.date)
    assertEquals(2, result.count)
    assertEquals(2, result.totalHabits)
    assertEquals(1f, result.completionRatio)
    assertTrue(result.inRange)
  }

  @Test
  fun `builds day with partial completion`() {
    val date = LocalDate(2024, 1, 15)
    val habitsConfig =
      listOf(
        HabitConfig(tag = "#habits/exercise", label = "Exercise"),
        HabitConfig(tag = "#habits/reading", label = "Reading"),
        HabitConfig(tag = "#habits/meditation", label = "Meditation"),
      )
    val dailyMemo =
      DailyMemoInfo(
        name = "test",
        content = "#daily 2024-01-15\n#habits/exercise",
      )

    val result = useCase(date, habitsConfig, dailyMemo)

    assertNotNull(result)
    assertEquals(1, result.count)
    assertEquals(3, result.totalHabits)
    assertEquals(0.333f, result.completionRatio, 0.01f)
  }

  @Test
  fun `builds day with no completed habits`() {
    val date = LocalDate(2024, 1, 15)
    val habitsConfig =
      listOf(
        HabitConfig(tag = "#habits/exercise", label = "Exercise"),
      )
    val dailyMemo =
      DailyMemoInfo(
        name = "test",
        content = "#daily 2024-01-15",
      )

    val result = useCase(date, habitsConfig, dailyMemo)

    assertNotNull(result)
    assertEquals(0, result.count)
    assertEquals(1, result.totalHabits)
    assertEquals(0f, result.completionRatio)
  }

  @Test
  fun `builds day without daily memo`() {
    val date = LocalDate(2024, 1, 15)
    val habitsConfig =
      listOf(
        HabitConfig(tag = "#habits/exercise", label = "Exercise"),
      )

    val result = useCase(date, habitsConfig, null)

    assertNotNull(result)
    assertEquals(0, result.count)
    assertEquals(1, result.totalHabits)
    assertEquals(0f, result.completionRatio)
    assertNull(result.dailyMemo)
  }

  @Test
  fun `stores habit statuses correctly`() {
    val date = LocalDate(2024, 1, 15)
    val habitsConfig =
      listOf(
        HabitConfig(tag = "#habits/exercise", label = "Exercise"),
        HabitConfig(tag = "#habits/reading", label = "Reading"),
      )
    val dailyMemo =
      DailyMemoInfo(
        name = "test",
        content = "#daily\n#habits/exercise",
      )

    val result = useCase(date, habitsConfig, dailyMemo)

    assertNotNull(result)
    assertEquals(2, result.habitStatuses.size)

    val exerciseStatus = result.habitStatuses.find { it.tag == "#habits/exercise" }
    assertNotNull(exerciseStatus)
    assertTrue(exerciseStatus.done)

    val readingStatus = result.habitStatuses.find { it.tag == "#habits/reading" }
    assertNotNull(readingStatus)
    assertTrue(!readingStatus.done)
  }

  @Test
  fun `clamps completion ratio to valid range`() {
    val date = LocalDate(2024, 1, 15)
    val habitsConfig =
      listOf(
        HabitConfig(tag = "#habits/exercise", label = "Exercise"),
      )
    val dailyMemo =
      DailyMemoInfo(
        name = "test",
        content = "#habits/exercise",
      )

    val result = useCase(date, habitsConfig, dailyMemo)

    assertNotNull(result)
    assertTrue(result.completionRatio >= 0f)
    assertTrue(result.completionRatio <= 1f)
  }
}
