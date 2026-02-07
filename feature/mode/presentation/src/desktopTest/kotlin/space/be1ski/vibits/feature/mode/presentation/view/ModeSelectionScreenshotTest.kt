package space.be1ski.vibits.feature.mode.presentation.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import space.be1ski.vibits.core.elm.test.RecordingFeature
import space.be1ski.vibits.core.ui.form.CredentialValidationError
import space.be1ski.vibits.core.ui.test.runAppUiTest
import space.be1ski.vibits.core.ui.test.saveScreenshot
import space.be1ski.vibits.core.ui.test.setThemedContent
import space.be1ski.vibits.feature.mode.presentation.action.ModeSelectionAction
import space.be1ski.vibits.feature.mode.presentation.effect.ModeSelectionEffect
import space.be1ski.vibits.feature.mode.presentation.state.ModeSelectionState
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class ModeSelectionScreenshotTest {
  private fun createFeature(state: ModeSelectionState = ModeSelectionState()) =
    RecordingFeature<
      ModeSelectionAction,
      ModeSelectionState,
      ModeSelectionEffect.Command,
      ModeSelectionEffect.Notification,
    >(state)

  @Test
  fun `when default state then captures mode selection screen`() =
    runAppUiTest {
      setThemedContent { ModeSelectionScreen(createFeature()) }

      onNodeWithTag(ModeSelectionTestTags.ONLINE_CARD).assertIsDisplayed()
      saveScreenshot("mode_selection_default")
    }

  @Test
  fun `when quick online dialog shown then captures dialog`() =
    runAppUiTest {
      setThemedContent { ModeSelectionScreen(createFeature(ModeSelectionState(showQuickOnlineDialog = true))) }

      onNodeWithTag(ModeSelectionTestTags.QUICK_ONLINE_DIALOG).assertIsDisplayed()
      saveScreenshot("mode_selection_quick_online_dialog")
    }

  @Test
  fun `when credentials dialog shown then captures dialog`() =
    runAppUiTest {
      setThemedContent { ModeSelectionScreen(createFeature(ModeSelectionState(showCredentialsDialog = true))) }

      onNodeWithTag(ModeSelectionTestTags.CREDENTIALS_DIALOG).assertIsDisplayed()
      saveScreenshot("mode_selection_credentials_dialog")
    }

  @Test
  fun `when credentials dialog has error then captures error state`() =
    runAppUiTest {
      val feature =
        createFeature(
          ModeSelectionState(
            showCredentialsDialog = true,
            error = CredentialValidationError.CONNECTION_FAILED,
          ),
        )
      setThemedContent { ModeSelectionScreen(feature) }

      onNodeWithTag(ModeSelectionTestTags.CREDENTIALS_DIALOG).assertIsDisplayed()
      saveScreenshot("mode_selection_credentials_error")
    }
}
