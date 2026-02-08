@file:Suppress("LongMethod")

package space.be1ski.vibits.feature.homescreen.presentation.view

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import space.be1ski.vibits.core.elm.test.RecordingFeature
import space.be1ski.vibits.core.platform.locale.AppLanguage
import space.be1ski.vibits.core.platform.mode.AppMode
import space.be1ski.vibits.core.ui.test.runAppUiTest
import space.be1ski.vibits.core.ui.test.saveScreenshot
import space.be1ski.vibits.core.ui.test.setThemedContent
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
import space.be1ski.vibits.feature.habits.presentation.view.StatsTestTags
import space.be1ski.vibits.feature.homescreen.domain.model.AppState
import space.be1ski.vibits.feature.homescreen.domain.model.Screen
import space.be1ski.vibits.feature.homescreen.presentation.AppFeatures
import space.be1ski.vibits.feature.memos.domain.model.ExportResult
import space.be1ski.vibits.feature.memos.domain.model.Memo
import space.be1ski.vibits.feature.memos.domain.repository.ExportService
import space.be1ski.vibits.feature.memos.presentation.state.MemosState
import space.be1ski.vibits.feature.memos.presentation.view.FeedTestTags
import space.be1ski.vibits.feature.mode.presentation.state.ModeSelectionState
import space.be1ski.vibits.feature.onboarding.presentation.state.OnboardingState
import space.be1ski.vibits.feature.settings.domain.model.AppTheme
import space.be1ski.vibits.feature.settings.domain.model.TimeRangeTab
import space.be1ski.vibits.feature.settings.presentation.state.SettingsState
import space.be1ski.vibits.feature.settings.presentation.view.SettingsTestTags
import space.be1ski.vibits.feature.sync.domain.model.ConflictType
import space.be1ski.vibits.feature.sync.domain.model.SyncConflict
import space.be1ski.vibits.feature.sync.domain.model.SyncOperation
import space.be1ski.vibits.feature.sync.domain.model.SyncOperationType
import kotlin.test.Test
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

@OptIn(ExperimentalTestApi::class)
class AppShellScreenshotTest {
  private val configDate = LocalDate(2024, 1, 1)
  private val configInstant = kotlin.time.Instant.fromEpochMilliseconds(1_704_067_200_000) // 2024-01-01T00:00:00Z
  private val timeZone = TimeZone.UTC
  private val today = LocalDate(2024, 12, 15)

  private val exerciseConfig = HabitConfig(tag = "#habits/exercise", label = "Exercise", color = HabitColor(0xFF4CAF50))
  private val waterConfig = HabitConfig(tag = "#habits/water", label = "Water", color = HabitColor(0xFF2196F3))
  private val readingConfig = HabitConfig(tag = "#habits/reading", label = "Reading", color = HabitColor(0xFFFF9800))
  private val meditationConfig = HabitConfig(tag = "#habits/meditation", label = "Meditation", color = HabitColor(0xFF9C27B0))
  private val allHabits = listOf(exerciseConfig, waterConfig, readingConfig, meditationConfig)

  private val fakeExportService =
    object : ExportService {
      override fun exportLogs() = ExportResult.Success("/fake")

      override fun exportMemos() = ExportResult.Success("/fake")
    }

  private val demoMemos: List<Memo> by lazy { generateDemoMemos() }
  private val buildActivityData = BuildActivityDataUseCase(BuildDayDataUseCase())

  // region Demo data generation

  private fun generateDemoMemos(): List<Memo> {
    val memos = mutableListOf<Memo>()
    val rng = kotlin.random.Random(42)

    // Config memo
    val configContent =
      buildString {
        appendLine("#habits/config")
        appendLine()
        allHabits.forEach { h ->
          appendLine("${h.label} | ${h.tag} | #${h.color.argb.toString(16).takeLast(6).uppercase()}")
        }
      }
    memos.add(Memo(name = "memos/1", content = configContent, createTime = configInstant))

    // Daily tracking memos for ~11 months (Jan 2024 – Dec 2024)
    var id = 2
    var cursor = configDate
    while (cursor <= today) {
      val completedHabits =
        allHabits.filter { rng.nextFloat() < 0.75f }
      if (completedHabits.isNotEmpty()) {
        val dayStr = cursor.toString()
        val content =
          buildString {
            appendLine("#habits/daily $dayStr")
            appendLine()
            completedHabits.forEach { h -> appendLine(h.tag) }
          }
        val dayOffset = cursor.toEpochDays() - configDate.toEpochDays()
        memos.add(
          Memo(
            name = "memos/$id",
            content = content,
            createTime = configInstant + dayOffset.days + 8.hours,
          ),
        )
        id++
      }

      // Occasional regular posts
      if (rng.nextFloat() < 0.3f) {
        val content = "Day note for $cursor - ${listOf("Great day!", "Feeling productive", "Need more rest", "Good progress").random(rng)}"
        val dayOffset = cursor.toEpochDays() - configDate.toEpochDays()
        memos.add(
          Memo(
            name = "memos/$id",
            content = content,
            createTime = configInstant + dayOffset.days + 12.hours,
          ),
        )
        id++
      }

      cursor = LocalDate.fromEpochDays(cursor.toEpochDays() + 1)
    }
    return memos
  }

  private fun computeCache(
    memos: List<Memo>,
    range: ActivityRange,
    mode: ActivityMode,
    appMode: AppMode = AppMode.DEMO,
  ): Pair<ActivityCacheKey, CachedActivity> {
    val configTimeline = ExtractHabitsConfigUseCase(memos, timeZone)
    val dailyMemos = ExtractDailyMemosUseCase(memos, timeZone)
    val weekData =
      buildActivityData.buildWeekData(
        configTimeline = if (mode == ActivityMode.HABITS) configTimeline else emptyList(),
        dailyMemos = dailyMemos,
        timeZone = timeZone,
        memos = memos,
        range = range,
        mode = mode,
        today = today,
      )
    val configStartDate = configTimeline.firstOrNull()?.date
    val successRate =
      if (mode == ActivityMode.HABITS && configTimeline.isNotEmpty()) {
        CalculateSuccessRateUseCase(weekData, range, today, configStartDate)
      } else {
        null
      }
    val key = ActivityCacheKey(range, mode, appMode)
    val cached = CachedActivity(weekData, configTimeline, successRate)
    return key to cached
  }

  // endregion

  // region Helpers

  private fun ComposeUiTest.setVibitsApp(
    appState: AppState,
    memosState: MemosState = MemosState(memos = demoMemos, initialDataLoaded = true),
    habitsState: HabitsState = HabitsState(),
    settingsState: SettingsState = SettingsState(),
  ) {
    val features =
      AppFeatures(
        app = RecordingFeature(appState),
        memos = RecordingFeature(memosState),
        habits = RecordingFeature(habitsState),
        settings = RecordingFeature(settingsState),
      )
    setContent {
      VibitsApp(
        features = features,
        currentTheme = AppTheme.SYSTEM,
        currentLanguage = AppLanguage.ENGLISH,
        exportService = fakeExportService,
      )
    }
  }

  private fun habitsAppState(
    tab: TimeRangeTab = TimeRangeTab.WEEKS,
    periodStartDate: LocalDate = LocalDate(2024, 12, 9),
  ) = AppState(
    appMode = AppMode.DEMO,
    selectedScreen = Screen.HABITS,
    habitsTimeRangeTab = tab,
    periodStartDate = periodStartDate,
    autoLoaded = true,
  )

  private fun postsAppState(
    tab: TimeRangeTab = TimeRangeTab.WEEKS,
    periodStartDate: LocalDate = LocalDate(2024, 12, 9),
  ) = AppState(
    appMode = AppMode.DEMO,
    selectedScreen = Screen.STATS,
    postsTimeRangeTab = tab,
    periodStartDate = periodStartDate,
    autoLoaded = true,
  )

  private fun feedAppState() =
    AppState(
      appMode = AppMode.DEMO,
      selectedScreen = Screen.FEED,
      periodStartDate = LocalDate(2024, 12, 9),
      autoLoaded = true,
    )

  @OptIn(ExperimentalTestApi::class)
  private fun ComposeUiTest.setSettingsApp(settingsState: SettingsState) {
    setVibitsApp(appState = habitsAppState(), settingsState = settingsState)
  }

  private fun habitsStateWithCache(
    range: ActivityRange,
    mode: ActivityMode = ActivityMode.HABITS,
    extraCache: Map<ActivityCacheKey, CachedActivity> = emptyMap(),
  ): HabitsState {
    val (key, cached) = computeCache(demoMemos, range, mode)
    return HabitsState(activityDataCache = extraCache + (key to cached))
  }

  // endregion

  // region Loading

  @Test
  fun `when app is loading then captures loading screen`() =
    runAppUiTest {
      val features =
        AppFeatures(
          app = RecordingFeature(AppState(appMode = AppMode.DEMO, periodStartDate = today)),
          memos = RecordingFeature(MemosState(initialDataLoaded = false)),
          habits = RecordingFeature(HabitsState()),
          settings = RecordingFeature(SettingsState()),
        )
      setThemedContent {
        AppContent(
          appMode = AppMode.DEMO,
          showOnboarding = false,
          featuresState =
            FeaturesState(
              modeSelection = RecordingFeature(ModeSelectionState()),
              onboarding = RecordingFeature(OnboardingState()),
              app = features,
            ),
          appTheme = AppTheme.SYSTEM,
          appLanguage = AppLanguage.ENGLISH,
          exportService = fakeExportService,
          onResetApp = {},
          onThemeChanged = {},
          onLanguageChanged = {},
        )
      }

      onNodeWithTag(AppShellTestTags.LOADING_SCREEN).assertIsDisplayed()
      saveScreenshot("app_loading")
    }

  // endregion

  // region Habits stats

  @Test
  fun `when habits week view then captures week habits`() =
    runAppUiTest {
      val periodStart = LocalDate(2024, 12, 9)
      val range = ActivityRange.Week(periodStart)
      setVibitsApp(
        appState = habitsAppState(tab = TimeRangeTab.WEEKS, periodStartDate = periodStart),
        habitsState = habitsStateWithCache(range),
      )

      onNodeWithTag(AppShellTestTags.BOTTOM_NAV_HABITS).assertIsDisplayed()
      saveScreenshot("app_habits_week")
    }

  @Test
  fun `when habits month view then captures month habits`() =
    runAppUiTest {
      val periodStart = LocalDate(2024, 11, 1)
      val range = ActivityRange.Month(2024, Month.NOVEMBER)
      setVibitsApp(
        appState = habitsAppState(tab = TimeRangeTab.MONTHS, periodStartDate = periodStart),
        habitsState = habitsStateWithCache(range),
      )

      onNodeWithTag(AppShellTestTags.BOTTOM_NAV_HABITS).assertIsDisplayed()
      saveScreenshot("app_habits_month")
    }

  @Test
  fun `when habits quarter view then captures quarter habits`() =
    runAppUiTest {
      val periodStart = LocalDate(2024, 10, 1)
      val range = ActivityRange.Quarter(2024, 4)
      setVibitsApp(
        appState = habitsAppState(tab = TimeRangeTab.QUARTERS, periodStartDate = periodStart),
        habitsState = habitsStateWithCache(range),
      )

      onNodeWithTag(AppShellTestTags.BOTTOM_NAV_HABITS).assertIsDisplayed()
      saveScreenshot("app_habits_quarter")
    }

  @Test
  fun `when habits year view then captures year habits`() =
    runAppUiTest {
      val periodStart = LocalDate(2024, 6, 15)
      val range = ActivityRange.Year(2024)
      setVibitsApp(
        appState = habitsAppState(tab = TimeRangeTab.YEARS, periodStartDate = periodStart),
        habitsState = habitsStateWithCache(range),
      )

      onNodeWithTag(AppShellTestTags.BOTTOM_NAV_HABITS).assertIsDisplayed()
      saveScreenshot("app_habits_year")
    }

  // endregion

  // region Posts stats

  @Test
  fun `when posts week view then captures week posts`() =
    runAppUiTest {
      val periodStart = LocalDate(2024, 12, 9)
      val range = ActivityRange.Week(periodStart)
      setVibitsApp(
        appState = postsAppState(tab = TimeRangeTab.WEEKS, periodStartDate = periodStart),
        habitsState = habitsStateWithCache(range, mode = ActivityMode.POSTS),
      )

      onNodeWithTag(AppShellTestTags.BOTTOM_NAV_STATS).assertIsDisplayed()
      saveScreenshot("app_stats_week")
    }

  @Test
  fun `when posts month view then captures month posts`() =
    runAppUiTest {
      val periodStart = LocalDate(2024, 11, 1)
      val range = ActivityRange.Month(2024, Month.NOVEMBER)
      setVibitsApp(
        appState = postsAppState(tab = TimeRangeTab.MONTHS, periodStartDate = periodStart),
        habitsState = habitsStateWithCache(range, mode = ActivityMode.POSTS),
      )

      onNodeWithTag(AppShellTestTags.BOTTOM_NAV_STATS).assertIsDisplayed()
      saveScreenshot("app_stats_month")
    }

  // endregion

  // region Feed

  @Test
  fun `when feed has data then captures feed screen`() =
    runAppUiTest {
      setVibitsApp(appState = feedAppState())

      onNodeWithTag(AppShellTestTags.BOTTOM_NAV_FEED).assertIsDisplayed()
      saveScreenshot("app_feed")
    }

  @Test
  fun `when feed is empty then captures empty feed`() =
    runAppUiTest {
      setVibitsApp(
        appState = feedAppState(),
        memosState = MemosState(memos = emptyList(), initialDataLoaded = true),
      )

      onNodeWithTag(AppShellTestTags.BOTTOM_NAV_FEED).assertIsDisplayed()
      saveScreenshot("app_feed_empty")
    }

  // endregion

  // region Settings dialogs

  @Test
  fun `when settings open in online mode then captures online settings`() =
    runAppUiTest {
      setSettingsApp(
        SettingsState(
          isOpen = true,
          appMode = AppMode.ONLINE,
          editBaseUrl = "https://memos.example.com",
          editToken = "test-token-123",
        ),
      )

      onNodeWithTag(SettingsTestTags.SETTINGS_DIALOG).assertIsDisplayed()
      saveScreenshot("app_settings_online")
    }

  @Test
  fun `when settings open in demo mode then captures demo settings`() =
    runAppUiTest {
      setSettingsApp(SettingsState(isOpen = true, appMode = AppMode.DEMO))

      onNodeWithTag(SettingsTestTags.SETTINGS_DIALOG).assertIsDisplayed()
      saveScreenshot("app_settings_demo")
    }

  @Test
  fun `when logs dialog open then captures logs`() =
    runAppUiTest {
      setSettingsApp(SettingsState(isOpen = true, appMode = AppMode.DEMO, showLogsDialog = true))

      onNodeWithTag(SettingsTestTags.LOGS_DIALOG).assertIsDisplayed()
      saveScreenshot("app_settings_logs")
    }

  @Test
  fun `when reset confirmation open then captures reset dialog`() =
    runAppUiTest {
      setSettingsApp(SettingsState(isOpen = true, appMode = AppMode.DEMO, showResetConfirmation = true))

      onNodeWithTag(SettingsTestTags.RESET_OPTIONS_DIALOG).assertIsDisplayed()
      saveScreenshot("app_settings_reset")
    }

  // endregion

  // region Habit dialogs

  @Test
  fun `when habit editor open then captures editor dialog`() =
    runAppUiTest {
      val editorDay =
        ContributionDay(
          date = today,
          count = 1,
          totalHabits = 4,
          completionRatio = 0.25f,
          habitStatuses =
            listOf(
              HabitStatus(tag = "#habits/exercise", label = "Exercise", done = true),
              HabitStatus(tag = "#habits/water", label = "Water", done = false),
              HabitStatus(tag = "#habits/reading", label = "Reading", done = false),
              HabitStatus(tag = "#habits/meditation", label = "Meditation", done = false),
            ),
          dailyMemo = null,
          inRange = true,
        )
      val range = ActivityRange.Week(LocalDate(2024, 12, 9))
      setVibitsApp(
        appState = habitsAppState(),
        habitsState =
          habitsStateWithCache(range).copy(
            editorDay = editorDay,
            editorConfig = allHabits,
            editorSelections =
              mapOf(
                "#habits/exercise" to true,
                "#habits/water" to false,
                "#habits/reading" to false,
                "#habits/meditation" to false,
              ),
          ),
      )

      onNodeWithTag(AppShellTestTags.HABIT_EDITOR_DIALOG).assertIsDisplayed()
      saveScreenshot("app_habit_editor")
    }

  @Test
  fun `when config dialog open then captures habits config`() =
    runAppUiTest {
      val range = ActivityRange.Week(LocalDate(2024, 12, 9))
      setVibitsApp(
        appState = habitsAppState(),
        habitsState =
          habitsStateWithCache(range).copy(
            showConfigDialog = true,
            editingHabits =
              allHabits.mapIndexed { i, h ->
                EditableHabit(id = "$i", tag = h.tag, label = h.label, color = h.color)
              },
          ),
      )

      onNodeWithTag(StatsTestTags.HABITS_CONFIG_DIALOG).assertIsDisplayed()
      saveScreenshot("app_habits_config")
    }

  @Test
  fun `when delete confirm shown then captures delete dialog`() =
    runAppUiTest {
      val testDay =
        ContributionDay(
          date = today,
          count = 2,
          totalHabits = 4,
          completionRatio = 0.5f,
          habitStatuses =
            listOf(
              HabitStatus(tag = "#habits/exercise", label = "Exercise", done = true),
              HabitStatus(tag = "#habits/water", label = "Water", done = true),
              HabitStatus(tag = "#habits/reading", label = "Reading", done = false),
              HabitStatus(tag = "#habits/meditation", label = "Meditation", done = false),
            ),
          dailyMemo = null,
          inRange = true,
        )
      val range = ActivityRange.Week(LocalDate(2024, 12, 9))
      setVibitsApp(
        appState = habitsAppState(),
        habitsState =
          habitsStateWithCache(range).copy(
            showDeleteConfirm = true,
            editorDay = testDay,
          ),
      )

      saveScreenshot("app_habits_delete")
    }

  @Test
  fun `when single toggle shown then captures toggle dialog`() =
    runAppUiTest {
      val testDay =
        ContributionDay(
          date = today,
          count = 1,
          totalHabits = 4,
          completionRatio = 0.25f,
          habitStatuses =
            listOf(
              HabitStatus(tag = "#habits/exercise", label = "Exercise", done = true),
              HabitStatus(tag = "#habits/water", label = "Water", done = false),
              HabitStatus(tag = "#habits/reading", label = "Reading", done = false),
              HabitStatus(tag = "#habits/meditation", label = "Meditation", done = false),
            ),
          dailyMemo = null,
          inRange = true,
        )
      val range = ActivityRange.Week(LocalDate(2024, 12, 9))
      setVibitsApp(
        appState = habitsAppState(),
        habitsState =
          habitsStateWithCache(range).copy(
            singleToggleDay = testDay,
            singleToggleHabitTag = "#habits/water",
            singleToggleHabitLabel = "Water",
            singleToggleConfig = allHabits,
          ),
      )

      onNodeWithTag(StatsTestTags.SINGLE_TOGGLE_DIALOG).assertIsDisplayed()
      saveScreenshot("app_habits_toggle")
    }

  // endregion

  // region Feed dialogs

  @Test
  fun `when edit config warning shown then captures warning dialog`() =
    runAppUiTest {
      setVibitsApp(
        appState = feedAppState(),
        habitsState =
          HabitsState(showEditConfigWarning = true),
      )

      onNodeWithTag(StatsTestTags.EDIT_CONFIG_WARNING_DIALOG).assertIsDisplayed()
      saveScreenshot("app_feed_edit_warning")
    }

  @Test
  fun `when sync conflict dialog shown then captures conflict dialog`() =
    runAppUiTest {
      setVibitsApp(
        appState = feedAppState(),
        memosState =
          MemosState(
            memos = demoMemos,
            initialDataLoaded = true,
            showConflictDialog = true,
            syncConflicts =
              listOf(
                SyncConflict(
                  operation =
                    SyncOperation(
                      id = "op1",
                      type = SyncOperationType.UPDATE,
                      memoName = demoMemos.first().name,
                    ),
                  localMemo = demoMemos.first(),
                  serverMemo = demoMemos.first(),
                  conflictType = ConflictType.BOTH_MODIFIED,
                ),
              ),
          ),
      )

      onNodeWithTag(FeedTestTags.SYNC_CONFLICT_DIALOG).assertIsDisplayed()
      saveScreenshot("app_sync_conflict")
    }

  // endregion
}
