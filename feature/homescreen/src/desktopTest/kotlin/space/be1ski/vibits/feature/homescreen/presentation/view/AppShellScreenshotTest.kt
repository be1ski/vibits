package space.be1ski.vibits.feature.homescreen.presentation.view

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import kotlinx.datetime.LocalDate
import space.be1ski.vibits.core.elm.test.RecordingFeature
import space.be1ski.vibits.core.platform.locale.AppLanguage
import space.be1ski.vibits.core.platform.mode.AppMode
import space.be1ski.vibits.core.ui.test.runAppUiTest
import space.be1ski.vibits.core.ui.test.saveScreenshot
import space.be1ski.vibits.core.ui.test.setThemedContent
import space.be1ski.vibits.feature.habits.presentation.state.HabitsState
import space.be1ski.vibits.feature.homescreen.domain.model.AppState
import space.be1ski.vibits.feature.homescreen.domain.model.Screen
import space.be1ski.vibits.feature.homescreen.presentation.AppFeatures
import space.be1ski.vibits.feature.memos.domain.model.ExportResult
import space.be1ski.vibits.feature.memos.domain.repository.ExportService
import space.be1ski.vibits.feature.memos.presentation.state.MemosState
import space.be1ski.vibits.feature.mode.presentation.state.ModeSelectionState
import space.be1ski.vibits.feature.onboarding.presentation.state.OnboardingState
import space.be1ski.vibits.feature.settings.domain.model.AppTheme
import space.be1ski.vibits.feature.settings.presentation.state.SettingsState
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class AppShellScreenshotTest {
  private val testDate = LocalDate(2025, 1, 6)

  private val fakeExportService =
    object : ExportService {
      override fun exportLogs() = ExportResult.Success("/fake")

      override fun exportMemos() = ExportResult.Success("/fake")
    }

  private fun createAppFeatures(
    appState: AppState = AppState(appMode = AppMode.DEMO, periodStartDate = testDate),
    memosState: MemosState = MemosState(initialDataLoaded = true),
  ) = AppFeatures(
    app = RecordingFeature(appState),
    memos = RecordingFeature(memosState),
    habits = RecordingFeature(HabitsState()),
    settings = RecordingFeature(SettingsState()),
  )

  @OptIn(ExperimentalTestApi::class)
  private fun ComposeUiTest.setVibitsApp(selectedScreen: Screen) {
    val features =
      createAppFeatures(
        appState =
          AppState(
            appMode = AppMode.DEMO,
            selectedScreen = selectedScreen,
            periodStartDate = testDate,
            autoLoaded = true,
          ),
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

  @Test
  fun `when app is loading then captures loading screen`() =
    runAppUiTest {
      val features = createAppFeatures(memosState = MemosState(initialDataLoaded = false))
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
      saveScreenshot("homescreen", "AppShellScreenshotTest", "app_loading")
    }

  @Test
  fun `when habits tab selected then captures habits screen`() =
    runAppUiTest {
      setVibitsApp(Screen.HABITS)
      onNodeWithTag(AppShellTestTags.BOTTOM_NAV_HABITS).assertIsDisplayed()
      saveScreenshot("homescreen", "AppShellScreenshotTest", "app_habits_tab")
    }

  @Test
  fun `when stats tab selected then captures stats screen`() =
    runAppUiTest {
      setVibitsApp(Screen.STATS)
      onNodeWithTag(AppShellTestTags.BOTTOM_NAV_STATS).assertIsDisplayed()
      saveScreenshot("homescreen", "AppShellScreenshotTest", "app_stats_tab")
    }

  @Test
  fun `when feed tab selected then captures feed screen`() =
    runAppUiTest {
      setVibitsApp(Screen.FEED)
      onNodeWithTag(AppShellTestTags.BOTTOM_NAV_FEED).assertIsDisplayed()
      saveScreenshot("homescreen", "AppShellScreenshotTest", "app_feed_tab")
    }
}
