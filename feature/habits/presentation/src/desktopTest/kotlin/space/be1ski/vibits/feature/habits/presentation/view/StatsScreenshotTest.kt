package space.be1ski.vibits.feature.habits.presentation.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import space.be1ski.vibits.core.platform.mode.AppMode
import space.be1ski.vibits.core.ui.Indent
import space.be1ski.vibits.core.ui.date.DateFormatter
import space.be1ski.vibits.core.ui.test.runAppUiTest
import space.be1ski.vibits.core.ui.test.saveScreenshot
import space.be1ski.vibits.core.ui.test.setThemedContent
import space.be1ski.vibits.feature.habits.domain.formatHexColor
import space.be1ski.vibits.feature.habits.domain.model.ActivityMode
import space.be1ski.vibits.feature.habits.domain.model.ActivityRange
import space.be1ski.vibits.feature.habits.domain.model.CachedActivity
import space.be1ski.vibits.feature.habits.domain.model.ContributionDay
import space.be1ski.vibits.feature.habits.domain.model.HabitColor
import space.be1ski.vibits.feature.habits.domain.model.HabitConfig
import space.be1ski.vibits.feature.habits.domain.model.HabitStatus
import space.be1ski.vibits.feature.habits.domain.usecase.BuildActivityDataUseCase
import space.be1ski.vibits.feature.habits.domain.usecase.BuildDayDataUseCase
import space.be1ski.vibits.feature.habits.domain.usecase.CalculateSuccessRateUseCase
import space.be1ski.vibits.feature.habits.domain.usecase.ExtractDailyMemosUseCase
import space.be1ski.vibits.feature.habits.domain.usecase.ExtractHabitsConfigUseCase
import space.be1ski.vibits.feature.habits.presentation.state.ActivityCacheKey
import space.be1ski.vibits.feature.habits.presentation.state.EditableHabit
import space.be1ski.vibits.feature.habits.presentation.state.HabitsState
import space.be1ski.vibits.feature.habits.presentation.view.components.EditConfigWarningDialog
import space.be1ski.vibits.feature.habits.presentation.view.components.HabitsConfigDialog
import space.be1ski.vibits.feature.memos.domain.model.Memo
import space.be1ski.vibits.feature.memos.domain.model.PostTags
import kotlin.random.Random
import kotlin.test.Test
import kotlin.time.Instant

@OptIn(ExperimentalTestApi::class)
@Suppress("LargeClass")
class StatsScreenshotTest {
  private val testDateFormatter =
    DateFormatter(
      months = Month.entries.associateWith { it.name.lowercase().take(3) },
      days = DayOfWeek.entries.associateWith { it.name.lowercase().take(3) },
    )

  private val today = LocalDate(2025, 1, 20) // Monday
  private val timeZone = TimeZone.UTC

  private val exerciseConfig = HabitConfig(tag = "#habits/exercise", label = "Exercise", color = HabitColor(0xFF4CAF50))
  private val readingConfig = HabitConfig(tag = "#habits/reading", label = "Reading", color = HabitColor(0xFFFF9800))
  private val meditationConfig = HabitConfig(tag = "#habits/meditation", label = "Meditation", color = HabitColor(0xFF9C27B0))
  private val waterConfig = HabitConfig(tag = "#habits/water", label = "Water", color = HabitColor(0xFF00BCD4))
  private val learningConfig = HabitConfig(tag = "#habits/learning", label = "Learning", color = HabitColor(0xFFE91E63))
  private val walkingConfig = HabitConfig(tag = "#habits/walking", label = "Walking", color = HabitColor(0xFF607D8B))
  private val noSugarConfig = HabitConfig(tag = "#habits/no_sugar", label = "no sugar", color = HabitColor(0xFFF44336))
  private val earlySleepConfig = HabitConfig(tag = "#habits/early_sleep", label = "early sleep", color = HabitColor(0xFF2196F3))

  private val allHabits =
    listOf(exerciseConfig, readingConfig, meditationConfig, waterConfig, learningConfig, walkingConfig, noSugarConfig, earlySleepConfig)

  private val demoMemos: List<Memo> by lazy { generateDeterministicDemoMemos() }

  @Suppress("MagicNumber")
  private fun generateDeterministicDemoMemos(): List<Memo> {
    val memos = mutableListOf<Memo>()
    val random = Random(42)
    val configDate = LocalDate(2024, 1, 1)
    val configContent =
      buildString {
        appendLine(PostTags.HABITS_CONFIG)
        appendLine()
        allHabits.forEach { habit -> appendLine("${habit.label} | ${habit.tag} | ${formatHexColor(habit.color)}") }
      }
    memos.add(Memo(name = "memos/demo_config", content = configContent.trim(), createTime = instantForDate(configDate, 8)))

    var current = configDate
    val rates = floatArrayOf(0.95f, 0.85f, 0.80f, 0.95f, 0.70f, 0.80f, 0.65f, 0.75f)
    while (current <= today) {
      val completed = allHabits.filterIndexed { i, _ -> random.nextFloat() < rates[i] }
      if (completed.isNotEmpty()) {
        val content =
          buildString {
            appendLine("${PostTags.HABITS_DAILY} $current")
            appendLine()
            completed.forEach { appendLine(it.tag) }
          }
        memos.add(Memo(name = "memos/demo_daily_$current", content = content.trim(), createTime = instantForDate(current, 9)))
      }
      if (random.nextFloat() < 0.15f) {
        memos.add(Memo(name = "memos/demo_post_$current", content = "Note for $current", createTime = instantForDate(current, 14)))
      }
      current = LocalDate.fromEpochDays(current.toEpochDays() + 1)
    }
    return memos
  }

  @Suppress("MagicNumber")
  private fun instantForDate(
    date: LocalDate,
    hour: Int,
  ): Instant {
    val epochSeconds = date.toEpochDays() * 86_400L + hour * 3_600
    return Instant.fromEpochSeconds(epochSeconds)
  }

  private fun computeCache(
    memos: List<Memo>,
    range: ActivityRange,
    mode: ActivityMode,
  ): CachedActivity {
    val buildActivityData = BuildActivityDataUseCase(BuildDayDataUseCase())
    val configTimeline = ExtractHabitsConfigUseCase(memos, timeZone)
    val dailyMemos = ExtractDailyMemosUseCase(memos, timeZone)
    val weekData = buildActivityData.buildWeekData(configTimeline, dailyMemos, timeZone, memos, range, mode, today)
    val successRate =
      if (mode == ActivityMode.HABITS) {
        CalculateSuccessRateUseCase(weekData, range, today, configTimeline.firstOrNull()?.date)
      } else {
        null
      }
    return CachedActivity(weekData, configTimeline, successRate)
  }

  private fun habitsStateWithCache(
    range: ActivityRange,
    mode: ActivityMode,
  ): HabitsState {
    val key = ActivityCacheKey(range, mode, AppMode.DEMO)
    return HabitsState(activityDataCache = mapOf(key to computeCache(demoMemos, range, mode)))
  }

  // --- Week Habits ---
  @Test
  fun `when week habits then captures week view`() =
    runAppUiTest {
      val range = ActivityRange.Week(startDate = LocalDate(2024, 12, 9))
      setThemedContent {
        Box(modifier = Modifier.padding(Indent.m)) {
          StatsScreen(
            state = StatsScreenState(memos = demoMemos, range = range, activityMode = ActivityMode.HABITS),
            appMode = AppMode.DEMO,
            dateFormatter = testDateFormatter,
            habitsState = habitsStateWithCache(range, ActivityMode.HABITS),
          )
        }
      }
      onNodeWithTag(StatsTestTags.STATS_SCREEN).assertIsDisplayed()
      saveScreenshot("stats_week_habits")
    }

  // --- Month Habits ---
  @Test
  fun `when month habits then captures month view`() =
    runAppUiTest {
      val range = ActivityRange.Month(2024, Month.NOVEMBER)
      setThemedContent {
        Box(modifier = Modifier.padding(Indent.m)) {
          StatsScreen(
            state = StatsScreenState(memos = demoMemos, range = range, activityMode = ActivityMode.HABITS),
            appMode = AppMode.DEMO,
            dateFormatter = testDateFormatter,
            habitsState = habitsStateWithCache(range, ActivityMode.HABITS),
          )
        }
      }
      onNodeWithTag(StatsTestTags.STATS_SCREEN).assertIsDisplayed()
      saveScreenshot("stats_month_habits")
    }

  // --- Quarter Habits ---
  @Test
  fun `when quarter habits then captures quarter view`() =
    runAppUiTest {
      val range = ActivityRange.Quarter(2024, 4)
      setThemedContent {
        Box(modifier = Modifier.padding(Indent.m)) {
          StatsScreen(
            state = StatsScreenState(memos = demoMemos, range = range, activityMode = ActivityMode.HABITS),
            appMode = AppMode.DEMO,
            dateFormatter = testDateFormatter,
            habitsState = habitsStateWithCache(range, ActivityMode.HABITS),
          )
        }
      }
      onNodeWithTag(StatsTestTags.STATS_SCREEN).assertIsDisplayed()
      saveScreenshot("stats_quarter_habits")
    }

  // --- Year Habits ---
  @Test
  fun `when year habits then captures year view`() =
    runAppUiTest {
      val range = ActivityRange.Year(2024)
      setThemedContent {
        Box(modifier = Modifier.padding(Indent.m)) {
          StatsScreen(
            state = StatsScreenState(memos = demoMemos, range = range, activityMode = ActivityMode.HABITS),
            appMode = AppMode.DEMO,
            dateFormatter = testDateFormatter,
            habitsState = habitsStateWithCache(range, ActivityMode.HABITS),
          )
        }
      }
      onNodeWithTag(StatsTestTags.STATS_SCREEN).assertIsDisplayed()
      saveScreenshot("stats_year_habits")
    }

  // --- Week Posts ---
  @Test
  fun `when week posts then captures posts week view`() =
    runAppUiTest {
      val range = ActivityRange.Week(startDate = LocalDate(2024, 12, 9))
      setThemedContent {
        Box(modifier = Modifier.padding(Indent.m)) {
          StatsScreen(
            state = StatsScreenState(memos = demoMemos, range = range, activityMode = ActivityMode.POSTS),
            appMode = AppMode.DEMO,
            dateFormatter = testDateFormatter,
            habitsState = habitsStateWithCache(range, ActivityMode.POSTS),
          )
        }
      }
      onNodeWithTag(StatsTestTags.STATS_SCREEN).assertIsDisplayed()
      saveScreenshot("stats_week_posts")
    }

  // --- Month Posts ---
  @Test
  fun `when month posts then captures posts month view`() =
    runAppUiTest {
      val range = ActivityRange.Month(2024, Month.NOVEMBER)
      setThemedContent {
        Box(modifier = Modifier.padding(Indent.m)) {
          StatsScreen(
            state = StatsScreenState(memos = demoMemos, range = range, activityMode = ActivityMode.POSTS),
            appMode = AppMode.DEMO,
            dateFormatter = testDateFormatter,
            habitsState = habitsStateWithCache(range, ActivityMode.POSTS),
          )
        }
      }
      onNodeWithTag(StatsTestTags.STATS_SCREEN).assertIsDisplayed()
      saveScreenshot("stats_month_posts")
    }

  // --- Config Dialog ---
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
      saveScreenshot("habits_config_dialog")
    }

  @Test
  fun `when demo mode then captures config dialog with localized labels`() =
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
          demoMode = true,
          dispatch = {},
        )
      }
      onNodeWithTag(StatsTestTags.HABITS_CONFIG_DIALOG).assertIsDisplayed()
      saveScreenshot("habits_config_dialog_demo")
    }

  // --- Edit Config Warning ---
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
      saveScreenshot("habits_edit_warning_dialog")
    }

  // --- Delete Confirm ---
  @Test
  fun `when delete confirm shown then captures delete dialog`() =
    runAppUiTest {
      val range = ActivityRange.Week(startDate = LocalDate(2024, 12, 9))
      val testDay =
        ContributionDay(
          date = LocalDate(2024, 12, 11),
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
      val cache = habitsStateWithCache(range, ActivityMode.HABITS)
      setThemedContent {
        Box(modifier = Modifier.padding(Indent.m)) {
          StatsScreen(
            state = StatsScreenState(memos = demoMemos, range = range, activityMode = ActivityMode.HABITS),
            appMode = AppMode.DEMO,
            dateFormatter = testDateFormatter,
            habitsState = cache.copy(showDeleteConfirm = true, editorDay = testDay),
          )
        }
      }
      onNodeWithTag(StatsTestTags.EMPTY_DELETE_DIALOG).assertIsDisplayed()
      saveScreenshot("habit_delete_confirm_dialog")
    }

  // --- Single Toggle ---
  @Test
  fun `when single toggle confirm shown then captures toggle dialog`() =
    runAppUiTest {
      val range = ActivityRange.Week(startDate = LocalDate(2024, 12, 9))
      val testDay =
        ContributionDay(
          date = LocalDate(2024, 12, 11),
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
      val cache = habitsStateWithCache(range, ActivityMode.HABITS)
      setThemedContent {
        Box(modifier = Modifier.padding(Indent.m)) {
          StatsScreen(
            state = StatsScreenState(memos = demoMemos, range = range, activityMode = ActivityMode.HABITS),
            appMode = AppMode.DEMO,
            dateFormatter = testDateFormatter,
            habitsState =
              cache.copy(
                singleToggleDay = testDay,
                singleToggleHabitTag = "#habits/water",
                singleToggleHabitLabel = "Water",
                singleToggleConfig = listOf(exerciseConfig, waterConfig),
              ),
          )
        }
      }
      onNodeWithTag(StatsTestTags.SINGLE_TOGGLE_DIALOG).assertIsDisplayed()
      saveScreenshot("single_habit_toggle_dialog")
    }
}
