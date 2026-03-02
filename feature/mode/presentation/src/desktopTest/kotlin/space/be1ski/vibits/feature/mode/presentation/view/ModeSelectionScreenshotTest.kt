package space.be1ski.vibits.feature.mode.presentation.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import space.be1ski.vibits.core.elm.test.RecordingFeature
import space.be1ski.vibits.core.ui.form.CredentialValidationError
import space.be1ski.vibits.core.ui.test.captureAllVariants
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
    captureAllVariants(
      "mode_selection_default",
      assertions = { onNodeWithTag(ModeSelectionTestTags.ONLINE_CARD).assertIsDisplayed() },
    ) { ModeSelectionScreen(createFeature()) }

  @Test
  fun `when quick online dialog shown then captures dialog`() =
    captureAllVariants(
      "mode_selection_quick_online_dialog",
      assertions = { onNodeWithTag(ModeSelectionTestTags.QUICK_ONLINE_DIALOG).assertIsDisplayed() },
    ) {
      ModeSelectionScreen(createFeature(ModeSelectionState(showQuickOnlineDialog = true)))
    }

  @Test
  fun `when credentials dialog shown then captures dialog`() =
    captureAllVariants(
      "mode_selection_credentials_dialog",
      assertions = { onNodeWithTag(ModeSelectionTestTags.CREDENTIALS_DIALOG).assertIsDisplayed() },
    ) {
      ModeSelectionScreen(createFeature(ModeSelectionState(showCredentialsDialog = true)))
    }

  @Test
  fun `when credentials dialog has error then captures error state`() =
    captureAllVariants(
      "mode_selection_credentials_error",
      assertions = { onNodeWithTag(ModeSelectionTestTags.CREDENTIALS_DIALOG).assertIsDisplayed() },
    ) {
      ModeSelectionScreen(
        createFeature(
          ModeSelectionState(
            showCredentialsDialog = true,
            error = CredentialValidationError.CONNECTION_FAILED,
          ),
        ),
      )
    }
}
