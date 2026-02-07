package space.be1ski.vibits.feature.habits.presentation.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import space.be1ski.vibits.core.platform.mode.AppMode
import space.be1ski.vibits.core.ui.date.DateFormatter
import space.be1ski.vibits.core.ui.test.runAppUiTest
import space.be1ski.vibits.core.ui.test.saveScreenshot
import space.be1ski.vibits.core.ui.test.setThemedContent
import space.be1ski.vibits.feature.habits.domain.model.ActivityMode
import space.be1ski.vibits.feature.habits.domain.model.ActivityRange
import space.be1ski.vibits.feature.habits.domain.model.ContributionDay
import space.be1ski.vibits.feature.habits.domain.model.HabitColor
import space.be1ski.vibits.feature.habits.domain.model.HabitConfig
import space.be1ski.vibits.feature.habits.domain.model.HabitStatus
import space.be1ski.vibits.feature.habits.presentation.state.EditableHabit
import space.be1ski.vibits.feature.habits.presentation.state.HabitsState
import space.be1ski.vibits.feature.habits.presentation.view.components.EditConfigWarningDialog
import space.be1ski.vibits.feature.habits.presentation.view.components.HabitsConfigDialog
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class StatsScreenshotTest {
  private val testDateFormatter =
    DateFormatter(
      months = Month.entries.associateWith { it.name.lowercase().take(3) },
      days = DayOfWeek.entries.associateWith { it.name.lowercase().take(3) },
    )

  private val testDate = LocalDate(2025, 1, 6)

  private val exerciseConfig = HabitConfig(tag = "#habits/exercise", label = "Exercise", color = HabitColor(0xFF4CAF50))
  private val waterConfig = HabitConfig(tag = "#habits/water", label = "Water", color = HabitColor(0xFF2196F3))

  @Test
  fun `when no habits configured then captures empty stats`() =
    runAppUiTest {
      setThemedContent {
        StatsScreen(
          state =
            StatsScreenState(
              memos = emptyList(),
              range = ActivityRange.Week(startDate = testDate),
              activityMode = ActivityMode.HABITS,
            ),
          appMode = AppMode.DEMO,
          dateFormatter = testDateFormatter,
        )
      }

      onNodeWithTag(StatsTestTags.STATS_SCREEN).assertIsDisplayed()
      saveScreenshot("habits", "StatsScreenshotTest", "stats_habits_empty")
    }

  @Test
  fun `when habits data present then captures stats with data`() =
    runAppUiTest {
      setThemedContent {
        StatsScreen(
          state =
            StatsScreenState(
              memos = emptyList(),
              range = ActivityRange.Week(startDate = testDate),
              activityMode = ActivityMode.HABITS,
            ),
          appMode = AppMode.DEMO,
          dateFormatter = testDateFormatter,
        )
      }

      onNodeWithTag(StatsTestTags.STATS_SCREEN).assertIsDisplayed()
      saveScreenshot("habits", "StatsScreenshotTest", "stats_habits_data")
    }

  @Test
  fun `when posts mode then captures posts stats`() =
    runAppUiTest {
      setThemedContent {
        StatsScreen(
          state =
            StatsScreenState(
              memos = emptyList(),
              range = ActivityRange.Week(startDate = testDate),
              activityMode = ActivityMode.POSTS,
            ),
          appMode = AppMode.DEMO,
          dateFormatter = testDateFormatter,
        )
      }

      onNodeWithTag(StatsTestTags.STATS_SCREEN).assertIsDisplayed()
      saveScreenshot("habits", "StatsScreenshotTest", "stats_posts_data")
    }

  @Test
  fun `when config dialog open then captures habits config`() =
    runAppUiTest {
      setThemedContent {
        HabitsConfigDialog(
          habitsState =
            HabitsState(
              showConfigDialog = true,
              editingHabits =
                listOf(
                  EditableHabit(id = "1", tag = "#habits/exercise", label = "Exercise", color = HabitColor(0xFF4CAF50)),
                  EditableHabit(id = "2", tag = "#habits/water", label = "Water", color = HabitColor(0xFF2196F3)),
                ),
            ),
          dispatch = {},
        )
      }

      onNodeWithTag(StatsTestTags.HABITS_CONFIG_DIALOG).assertIsDisplayed()
      saveScreenshot("habits", "StatsScreenshotTest", "habits_config_dialog")
    }

  @Test
  fun `when edit config warning shown then captures warning dialog`() =
    runAppUiTest {
      setThemedContent {
        EditConfigWarningDialog(
          habitsState = HabitsState(showEditConfigWarning = true),
          dispatch = {},
        )
      }

      onNodeWithTag(StatsTestTags.EDIT_CONFIG_WARNING_DIALOG).assertIsDisplayed()
      saveScreenshot("habits", "StatsScreenshotTest", "habits_edit_warning_dialog")
    }

  @Test
  fun `when delete confirm shown then captures delete dialog`() =
    runAppUiTest {
      val testDay =
        ContributionDay(
          date = testDate,
          count = 2,
          totalHabits = 2,
          completionRatio = 1f,
          habitStatuses =
            listOf(
              HabitStatus(tag = "#habits/exercise", label = "Exercise", done = true),
              HabitStatus(tag = "#habits/water", label = "Water", done = true),
            ),
          dailyMemo = null,
          inRange = true,
        )
      setThemedContent {
        StatsScreen(
          state =
            StatsScreenState(
              memos = emptyList(),
              range = ActivityRange.Week(startDate = testDate),
              activityMode = ActivityMode.HABITS,
            ),
          appMode = AppMode.DEMO,
          dateFormatter = testDateFormatter,
          habitsState =
            HabitsState(
              showDeleteConfirm = true,
              editorDay = testDay,
            ),
        )
      }

      onNodeWithTag(StatsTestTags.EMPTY_DELETE_DIALOG).assertIsDisplayed()
      saveScreenshot("habits", "StatsScreenshotTest", "habit_delete_confirm_dialog")
    }

  @Test
  fun `when single toggle confirm shown then captures toggle dialog`() =
    runAppUiTest {
      val testDay =
        ContributionDay(
          date = testDate,
          count = 1,
          totalHabits = 2,
          completionRatio = 0.5f,
          habitStatuses =
            listOf(
              HabitStatus(tag = "#habits/exercise", label = "Exercise", done = true),
              HabitStatus(tag = "#habits/water", label = "Water", done = false),
            ),
          dailyMemo = null,
          inRange = true,
        )
      setThemedContent {
        StatsScreen(
          state =
            StatsScreenState(
              memos = emptyList(),
              range = ActivityRange.Week(startDate = testDate),
              activityMode = ActivityMode.HABITS,
            ),
          appMode = AppMode.DEMO,
          dateFormatter = testDateFormatter,
          habitsState =
            HabitsState(
              singleToggleDay = testDay,
              singleToggleHabitTag = "#habits/water",
              singleToggleHabitLabel = "Water",
              singleToggleConfig = listOf(exerciseConfig, waterConfig),
            ),
        )
      }

      onNodeWithTag(StatsTestTags.SINGLE_TOGGLE_DIALOG).assertIsDisplayed()
      saveScreenshot("habits", "StatsScreenshotTest", "single_habit_toggle_dialog")
    }
}
