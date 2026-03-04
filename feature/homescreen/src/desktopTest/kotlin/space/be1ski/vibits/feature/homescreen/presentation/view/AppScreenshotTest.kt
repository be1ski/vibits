@file:Suppress("LongMethod")

package space.be1ski.vibits.feature.homescreen.presentation.view

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import space.be1ski.vibits.core.elm.test.RecordingFeature
import space.be1ski.vibits.core.platform.locale.AppLanguage
import space.be1ski.vibits.core.platform.logging.LogLevel
import space.be1ski.vibits.core.platform.mode.AppMode
import space.be1ski.vibits.core.ui.celebration.CelebrationAnimation
import space.be1ski.vibits.core.ui.celebration.CelebrationOverlay
import space.be1ski.vibits.core.ui.test.captureAllVariants
import space.be1ski.vibits.core.ui.test.runCompactUiTest
import space.be1ski.vibits.core.ui.test.runWideUiTest
import space.be1ski.vibits.core.ui.test.saveScreenshot
import space.be1ski.vibits.core.ui.theme.VibitsTheme
import space.be1ski.vibits.core.utils.logging.LogEntry
import space.be1ski.vibits.feature.changelog.domain.model.ChangelogEntry
import space.be1ski.vibits.feature.changelog.domain.test.FakeChangelogRepository
import space.be1ski.vibits.feature.changelog.domain.test.FakeLastSeenVersionStore
import space.be1ski.vibits.feature.changelog.domain.usecase.GetChangelogUseCase
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
import space.be1ski.vibits.feature.homescreen.domain.model.AppState
import space.be1ski.vibits.feature.homescreen.domain.model.Screen
import space.be1ski.vibits.feature.homescreen.presentation.AppFeatures
import space.be1ski.vibits.feature.memos.domain.model.ExportResult
import space.be1ski.vibits.feature.memos.domain.model.Memo
import space.be1ski.vibits.feature.memos.domain.repository.ExportService
import space.be1ski.vibits.feature.memos.presentation.state.MemosState
import space.be1ski.vibits.feature.mode.presentation.state.ModeSelectionState
import space.be1ski.vibits.feature.onboarding.presentation.state.OnboardingState
import space.be1ski.vibits.feature.settings.domain.model.AppTheme
import space.be1ski.vibits.feature.settings.domain.model.TimeRangeTab
import space.be1ski.vibits.feature.settings.presentation.state.SettingsState
import space.be1ski.vibits.feature.sync.domain.model.ConflictType
import space.be1ski.vibits.feature.sync.domain.model.SyncConflict
import space.be1ski.vibits.feature.sync.domain.model.SyncOperation
import space.be1ski.vibits.feature.sync.domain.model.SyncOperationType
import kotlin.test.Test
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

private const val CELEBRATION_SCREENSHOT_PROGRESS = 0.4f
private const val LOTTIE_LOAD_DELAY_MS = 2000L

@OptIn(ExperimentalTestApi::class)
class AppScreenshotTest {
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

  private val fakeGetChangelog = GetChangelogUseCase(FakeChangelogRepository(), FakeLastSeenVersionStore())

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
    darkTheme: Boolean = false,
    wideLayout: Boolean = true,
    currentVersion: String = "dev",
    getChangelog: GetChangelogUseCase = fakeGetChangelog,
    testLogs: List<LogEntry>? = null,
  ) {
    val features =
      AppFeatures(
        app = RecordingFeature(appState),
        memos = RecordingFeature(memosState),
        habits = RecordingFeature(habitsState),
        settings = RecordingFeature(settingsState),
      )
    setContent {
      VibitsTheme(darkTheme = darkTheme, wideLayout = wideLayout) {
        VibitsApp(
          features = features,
          currentTheme = AppTheme.SYSTEM,
          currentLanguage = AppLanguage.ENGLISH,
          exportService = fakeExportService,
          currentVersion = currentVersion,
          getChangelog = getChangelog,
          testLogs = testLogs,
        )
      }
    }
  }

  private fun ComposeUiTest.captureApp(
    name: String,
    appState: AppState,
    memosState: MemosState = MemosState(memos = demoMemos, initialDataLoaded = true),
    habitsState: HabitsState = HabitsState(),
    settingsState: SettingsState = SettingsState(),
    wideLayout: Boolean = true,
    currentVersion: String = "dev",
    getChangelog: GetChangelogUseCase = fakeGetChangelog,
    testLogs: List<LogEntry>? = null,
  ) {
    val platform = if (wideLayout) "wide" else "compact"
    setVibitsApp(
      appState,
      memosState,
      habitsState,
      settingsState,
      darkTheme = false,
      wideLayout = wideLayout,
      currentVersion = currentVersion,
      getChangelog = getChangelog,
      testLogs = testLogs,
    )
    saveScreenshot("${platform}_light_$name")
    setVibitsApp(
      appState,
      memosState,
      habitsState,
      settingsState,
      darkTheme = true,
      wideLayout = wideLayout,
      currentVersion = currentVersion,
      getChangelog = getChangelog,
      testLogs = testLogs,
    )
    saveScreenshot("${platform}_dark_$name")
  }

  private fun captureAppAllVariants(
    name: String,
    appState: AppState,
    memosState: MemosState = MemosState(memos = demoMemos, initialDataLoaded = true),
    habitsState: HabitsState = HabitsState(),
    settingsState: SettingsState = SettingsState(),
    currentVersion: String = "dev",
    getChangelog: GetChangelogUseCase = fakeGetChangelog,
    testLogs: List<LogEntry>? = null,
  ) {
    runWideUiTest {
      captureApp(
        name,
        appState,
        memosState,
        habitsState,
        settingsState,
        currentVersion = currentVersion,
        getChangelog = getChangelog,
        testLogs = testLogs,
      )
    }
    runCompactUiTest {
      captureApp(
        name,
        appState,
        memosState,
        habitsState,
        settingsState,
        wideLayout = false,
        currentVersion = currentVersion,
        getChangelog = getChangelog,
        testLogs = testLogs,
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

  private fun settingsAppState() =
    AppState(
      appMode = AppMode.DEMO,
      selectedScreen = Screen.SETTINGS,
      periodStartDate = LocalDate(2024, 12, 9),
      autoLoaded = true,
    )

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
  fun `when app is loading then captures loading screen`() {
    val features =
      AppFeatures(
        app = RecordingFeature(AppState(appMode = AppMode.DEMO, periodStartDate = today)),
        memos = RecordingFeature(MemosState(initialDataLoaded = false)),
        habits = RecordingFeature(HabitsState()),
        settings = RecordingFeature(SettingsState()),
      )
    captureAllVariants(
      "app_loading",
      assertions = { onNodeWithTag(AppShellTestTags.LOADING_SCREEN).assertIsDisplayed() },
    ) {
      AppContent(
        appMode = AppMode.DEMO,
        showOnboarding = false,
        justFinishedOnboarding = false,
        featuresState =
          FeaturesState(
            modeSelection = RecordingFeature(ModeSelectionState()),
            onboarding = RecordingFeature(OnboardingState()),
            app = features,
          ),
        appTheme = AppTheme.SYSTEM,
        appLanguage = AppLanguage.ENGLISH,
        exportService = fakeExportService,
        currentVersion = "dev",
        getChangelog = fakeGetChangelog,
        onResetApp = {},
        onThemeChanged = {},
        onLanguageChanged = {},
      )
    }
  }

  // endregion

  // region Habits stats

  @Test
  fun `when habits week view then captures week habits`() {
    val periodStart = LocalDate(2024, 12, 9)
    val range = ActivityRange.Week(periodStart)
    val appState = habitsAppState(tab = TimeRangeTab.WEEKS, periodStartDate = periodStart)
    val habitsState = habitsStateWithCache(range)
    captureAppAllVariants(name = "app_habits_week", appState = appState, habitsState = habitsState)
  }

  @Test
  fun `when habits month view then captures month habits`() {
    val periodStart = LocalDate(2024, 11, 1)
    val range = ActivityRange.Month(2024, Month.NOVEMBER)
    val appState = habitsAppState(tab = TimeRangeTab.MONTHS, periodStartDate = periodStart)
    val habitsState = habitsStateWithCache(range)
    captureAppAllVariants(name = "app_habits_month", appState = appState, habitsState = habitsState)
  }

  @Test
  fun `when habits quarter view then captures quarter habits`() {
    val periodStart = LocalDate(2024, 10, 1)
    val range = ActivityRange.Quarter(2024, 4)
    val appState = habitsAppState(tab = TimeRangeTab.QUARTERS, periodStartDate = periodStart)
    val habitsState = habitsStateWithCache(range)
    captureAppAllVariants(name = "app_habits_quarter", appState = appState, habitsState = habitsState)
  }

  @Test
  fun `when habits year view then captures year habits`() {
    val periodStart = LocalDate(2024, 6, 15)
    val range = ActivityRange.Year(2024)
    val appState = habitsAppState(tab = TimeRangeTab.YEARS, periodStartDate = periodStart)
    val habitsState = habitsStateWithCache(range)
    captureAppAllVariants(name = "app_habits_year", appState = appState, habitsState = habitsState)
  }

  // endregion

  // region Posts stats

  @Test
  fun `when posts week view then captures week posts`() {
    val periodStart = LocalDate(2024, 12, 9)
    val range = ActivityRange.Week(periodStart)
    val appState = postsAppState(tab = TimeRangeTab.WEEKS, periodStartDate = periodStart)
    val habitsState = habitsStateWithCache(range, mode = ActivityMode.POSTS)
    captureAppAllVariants(name = "app_stats_week", appState = appState, habitsState = habitsState)
  }

  @Test
  fun `when posts month view then captures month posts`() {
    val periodStart = LocalDate(2024, 11, 1)
    val range = ActivityRange.Month(2024, Month.NOVEMBER)
    val appState = postsAppState(tab = TimeRangeTab.MONTHS, periodStartDate = periodStart)
    val habitsState = habitsStateWithCache(range, mode = ActivityMode.POSTS)
    captureAppAllVariants(name = "app_stats_month", appState = appState, habitsState = habitsState)
  }

  // endregion

  // region Feed

  @Test
  fun `when feed has data then captures feed screen`() = captureAppAllVariants(name = "app_feed", appState = feedAppState())

  @Test
  fun `when feed is empty then captures empty feed`() =
    captureAppAllVariants(
      name = "app_feed_empty",
      appState = feedAppState(),
      memosState = MemosState(memos = emptyList(), initialDataLoaded = true),
    )

  // endregion

  // region Settings dialogs

  private fun captureSettingsVariants(
    name: String,
    settingsState: SettingsState,
    testLogs: List<LogEntry>? = null,
  ) {
    runWideUiTest {
      captureApp(
        name,
        appState = settingsAppState(),
        settingsState = settingsState,
        testLogs = testLogs,
      )
    }
    runCompactUiTest {
      captureApp(
        name,
        appState = habitsAppState(),
        settingsState = settingsState,
        wideLayout = false,
        testLogs = testLogs,
      )
    }
  }

  @Test
  fun `when settings open in online mode then captures online settings`() =
    captureSettingsVariants(
      name = "app_settings_online",
      settingsState =
        SettingsState(
          isOpen = true,
          appMode = AppMode.ONLINE,
          editBaseUrl = "https://memos.example.com",
          editToken = "test-token-123",
        ),
    )

  @Test
  fun `when settings open in demo mode then captures demo settings`() =
    captureSettingsVariants(
      name = "app_settings_demo",
      settingsState = SettingsState(isOpen = true, appMode = AppMode.DEMO),
    )

  @Test
  fun `when logs dialog open then captures logs`() =
    captureSettingsVariants(
      name = "app_settings_logs",
      settingsState = SettingsState(isOpen = true, appMode = AppMode.DEMO, showLogsDialog = true),
      testLogs =
        listOf(
          LogEntry(LocalDateTime(2024, 1, 1, 10, 0, 0), LogLevel.INFO, "App", "Application started"),
          LogEntry(LocalDateTime(2024, 1, 1, 10, 0, 1), LogLevel.INFO, "Sync", "Syncing memos..."),
          LogEntry(LocalDateTime(2024, 1, 1, 10, 0, 2), LogLevel.DEBUG, "Network", "GET /api/memos 200 OK"),
          LogEntry(LocalDateTime(2024, 1, 1, 10, 0, 3), LogLevel.WARN, "Cache", "Cache miss for key: habits"),
          LogEntry(LocalDateTime(2024, 1, 1, 10, 0, 4), LogLevel.INFO, "Sync", "Sync complete: 42 memos"),
        ),
    )

  @Test
  fun `when reset confirmation open then captures reset dialog`() =
    captureSettingsVariants(
      name = "app_settings_reset",
      settingsState = SettingsState(isOpen = true, appMode = AppMode.DEMO, showResetConfirmation = true),
    )

  // endregion

  // region Habit dialogs

  @Test
  fun `when habit editor open then captures editor dialog`() {
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
    captureAppAllVariants(
      name = "app_habit_editor",
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
  }

  @Test
  fun `when config dialog open then captures habits config`() {
    val range = ActivityRange.Week(LocalDate(2024, 12, 9))
    captureAppAllVariants(
      name = "app_habits_config",
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
  }

  @Test
  fun `when delete confirm shown then captures delete dialog`() {
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
    captureAppAllVariants(
      name = "app_habits_delete",
      appState = habitsAppState(),
      habitsState =
        habitsStateWithCache(range).copy(
          showDeleteConfirm = true,
          editorDay = testDay,
        ),
    )
  }

  @Test
  fun `when single toggle shown then captures toggle dialog`() {
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
    captureAppAllVariants(
      name = "app_habits_toggle",
      appState = habitsAppState(),
      habitsState =
        habitsStateWithCache(range).copy(
          singleToggleDay = testDay,
          singleToggleHabitTag = "#habits/water",
          singleToggleHabitLabel = "Water",
          singleToggleConfig = allHabits,
        ),
    )
  }

  // endregion

  // region Feed dialogs

  @Test
  fun `when edit config warning shown then captures warning dialog`() =
    captureAppAllVariants(
      name = "app_feed_edit_warning",
      appState = feedAppState(),
      habitsState = HabitsState(showEditConfigWarning = true),
    )

  @Test
  fun `when changelog dialog shown then captures changelog`() {
    val entries =
      listOf(
        ChangelogEntry(
          version = "1.2.0",
          title = "Release 1.2.0",
          body = "## Highlights\n* **Changelog dialog** on app upgrade\n* Improved sync reliability\n* Fixed habit streak calculation",
          date = "2026-03-01",
        ),
        ChangelogEntry(
          version = "1.1.0",
          title = "Release 1.1.0",
          body = "## Changes\n* Desktop sidebar layout\n* Dark mode improvements\n* Bug fixes",
          date = "2026-02-15",
        ),
      )

    fun freshChangelog() =
      GetChangelogUseCase(
        FakeChangelogRepository().apply { releasesResult = Result.success(entries) },
        FakeLastSeenVersionStore("1.0.0"),
      )
    val appState = habitsAppState()
    runWideUiTest {
      setVibitsApp(appState, currentVersion = "1.2.0", getChangelog = freshChangelog())
      saveScreenshot("wide_light_app_changelog")
      setVibitsApp(appState, darkTheme = true, currentVersion = "1.2.0", getChangelog = freshChangelog())
      saveScreenshot("wide_dark_app_changelog")
    }
    runCompactUiTest {
      setVibitsApp(appState, wideLayout = false, currentVersion = "1.2.0", getChangelog = freshChangelog())
      saveScreenshot("compact_light_app_changelog")
      setVibitsApp(appState, darkTheme = true, wideLayout = false, currentVersion = "1.2.0", getChangelog = freshChangelog())
      saveScreenshot("compact_dark_app_changelog")
    }
  }

  @Test
  fun `when sync conflict dialog shown then captures conflict dialog`() =
    captureAppAllVariants(
      name = "app_sync_conflict",
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

  // endregion

  // region Celebration

  @Test
  fun `when all habits completed then captures confetti celebration`() {
    val periodStart = LocalDate(2024, 12, 9)
    val range = ActivityRange.Week(periodStart)
    val appState = habitsAppState(tab = TimeRangeTab.WEEKS, periodStartDate = periodStart)
    val habitsState = habitsStateWithCache(range)
    runWideUiTest {
      captureCelebration("wide", appState, habitsState)
    }
    runCompactUiTest {
      captureCelebration("compact", appState, habitsState, wideLayout = false)
    }
  }

  private fun ComposeUiTest.captureCelebration(
    platform: String,
    appState: AppState,
    habitsState: HabitsState,
    wideLayout: Boolean = true,
  ) {
    // Preload Lottie composition (cold .lottie ZIP extraction is slow)
    setContent {
      CelebrationOverlay(
        animation = CelebrationAnimation.Confetti,
        onFinished = {},
        frozenProgress = CELEBRATION_SCREENSHOT_PROGRESS,
      )
    }
    @Suppress("BlockingMethodInNonBlockingContext")
    Thread.sleep(LOTTIE_LOAD_DELAY_MS)
    waitForIdle()

    for (darkTheme in listOf(false, true)) {
      val theme = if (darkTheme) "dark" else "light"
      val features =
        AppFeatures(
          app = RecordingFeature(appState),
          memos = RecordingFeature(MemosState(memos = demoMemos, initialDataLoaded = true)),
          habits = RecordingFeature(habitsState),
          settings = RecordingFeature(SettingsState()),
        )
      setContent {
        VibitsTheme(darkTheme = darkTheme, wideLayout = wideLayout) {
          VibitsApp(
            features = features,
            currentTheme = AppTheme.SYSTEM,
            currentLanguage = AppLanguage.ENGLISH,
            exportService = fakeExportService,
            currentVersion = "dev",
            getChangelog = fakeGetChangelog,
          )
          CelebrationOverlay(
            animation = CelebrationAnimation.Confetti,
            onFinished = {},
            frozenProgress = CELEBRATION_SCREENSHOT_PROGRESS,
          )
        }
      }
      waitForIdle()
      saveScreenshot("${platform}_${theme}_app_celebration_confetti")
    }
  }

  // endregion
}
