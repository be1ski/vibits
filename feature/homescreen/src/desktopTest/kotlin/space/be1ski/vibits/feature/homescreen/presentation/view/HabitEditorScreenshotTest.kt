package space.be1ski.vibits.feature.homescreen.presentation.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import kotlinx.datetime.LocalDate
import space.be1ski.vibits.core.platform.mode.AppMode
import space.be1ski.vibits.core.ui.test.runAppUiTest
import space.be1ski.vibits.core.ui.test.saveScreenshot
import space.be1ski.vibits.core.ui.test.setThemedContent
import space.be1ski.vibits.feature.habits.domain.model.ContributionDay
import space.be1ski.vibits.feature.habits.domain.model.HabitColor
import space.be1ski.vibits.feature.habits.domain.model.HabitConfig
import space.be1ski.vibits.feature.habits.presentation.state.HabitsState
import space.be1ski.vibits.feature.homescreen.domain.model.AppState
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class HabitEditorScreenshotTest {
  private val testDate = LocalDate(2025, 1, 6)

  @Test
  fun `when editor open then captures habit editor dialog`() =
    runAppUiTest {
      val editorDay =
        ContributionDay(
          date = testDate,
          count = 0,
          totalHabits = 2,
          completionRatio = 0f,
          habitStatuses = emptyList(),
          dailyMemo = null,
          inRange = true,
        )
      setThemedContent {
        HabitsDialogs(
          appState = AppState(appMode = AppMode.DEMO, periodStartDate = testDate),
          habitsState =
            HabitsState(
              editorDay = editorDay,
              editorConfig =
                listOf(
                  HabitConfig(tag = "#habits/exercise", label = "Exercise", color = HabitColor(0xFF4CAF50)),
                  HabitConfig(tag = "#habits/water", label = "Water", color = HabitColor(0xFF2196F3)),
                ),
              editorSelections =
                mapOf(
                  "#habits/exercise" to true,
                  "#habits/water" to false,
                ),
            ),
          dispatch = {},
        )
      }

      onNodeWithTag(AppShellTestTags.HABIT_EDITOR_DIALOG).assertIsDisplayed()
      saveScreenshot("habit_editor_dialog")
    }
}
