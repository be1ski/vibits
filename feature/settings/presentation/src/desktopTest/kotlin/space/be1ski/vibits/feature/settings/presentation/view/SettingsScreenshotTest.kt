package space.be1ski.vibits.feature.settings.presentation.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import space.be1ski.vibits.core.platform.mode.AppMode
import space.be1ski.vibits.core.ui.form.CredentialValidationError
import space.be1ski.vibits.core.ui.test.runAppUiTest
import space.be1ski.vibits.core.ui.test.saveScreenshot
import space.be1ski.vibits.core.ui.test.setThemedContent
import space.be1ski.vibits.feature.memos.domain.model.ExportResult
import space.be1ski.vibits.feature.memos.domain.repository.ExportService
import space.be1ski.vibits.feature.settings.presentation.state.SettingsState
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class SettingsScreenshotTest {
  private val fakeExportService =
    object : ExportService {
      override fun exportLogs() = ExportResult.Success("/fake")

      override fun exportMemos() = ExportResult.Success("/fake")
    }

  @Test
  fun `when online mode then captures online settings`() =
    runAppUiTest {
      setThemedContent {
        SettingsDialog(
          state =
            SettingsState(
              isOpen = true,
              appMode = AppMode.ONLINE,
              editBaseUrl = "https://memos.example.com",
              editToken = "test-token-123",
            ),
          dispatch = {},
          exportService = fakeExportService,
        )
      }

      onNodeWithTag(SettingsTestTags.SETTINGS_DIALOG).assertIsDisplayed()
      saveScreenshot("settings", "SettingsScreenshotTest", "settings_online")
    }

  @Test
  fun `when offline mode then captures offline settings`() =
    runAppUiTest {
      setThemedContent {
        SettingsDialog(
          state =
            SettingsState(
              isOpen = true,
              appMode = AppMode.OFFLINE,
            ),
          dispatch = {},
          exportService = fakeExportService,
        )
      }

      onNodeWithTag(SettingsTestTags.SETTINGS_DIALOG).assertIsDisplayed()
      saveScreenshot("settings", "SettingsScreenshotTest", "settings_offline")
    }

  @Test
  fun `when validation error then captures error state`() =
    runAppUiTest {
      setThemedContent {
        SettingsDialog(
          state =
            SettingsState(
              isOpen = true,
              appMode = AppMode.ONLINE,
              editBaseUrl = "https://memos.example.com",
              editToken = "",
              validationError = CredentialValidationError.CONNECTION_FAILED,
            ),
          dispatch = {},
          exportService = fakeExportService,
        )
      }

      onNodeWithTag(SettingsTestTags.SETTINGS_DIALOG).assertIsDisplayed()
      saveScreenshot("settings", "SettingsScreenshotTest", "settings_validation_error")
    }

  @Test
  fun `when logs dialog open then captures logs dialog`() =
    runAppUiTest {
      setThemedContent {
        SettingsDialog(
          state =
            SettingsState(
              isOpen = true,
              appMode = AppMode.DEMO,
              showLogsDialog = true,
            ),
          dispatch = {},
          exportService = fakeExportService,
        )
      }

      onNodeWithTag(SettingsTestTags.LOGS_DIALOG).assertIsDisplayed()
      saveScreenshot("settings", "SettingsScreenshotTest", "settings_logs_dialog")
    }

  @Test
  fun `when reset confirmation open then captures reset options dialog`() =
    runAppUiTest {
      setThemedContent {
        SettingsDialog(
          state =
            SettingsState(
              isOpen = true,
              appMode = AppMode.DEMO,
              showResetConfirmation = true,
            ),
          dispatch = {},
          exportService = fakeExportService,
        )
      }

      onNodeWithTag(SettingsTestTags.RESET_OPTIONS_DIALOG).assertIsDisplayed()
      saveScreenshot("settings", "SettingsScreenshotTest", "settings_reset_options_dialog")
    }
}
