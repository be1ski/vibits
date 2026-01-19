package space.be1ski.vibits.shared.feature.mode.presentation

import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode

sealed interface ModeSelectionAction {
  // Dialog lifecycle
  data object ShowCredentialsDialog : ModeSelectionAction

  data object DismissCredentialsDialog : ModeSelectionAction

  // Credentials input
  data class UpdateBaseUrl(
    val value: String,
  ) : ModeSelectionAction

  data class UpdateToken(
    val value: String,
  ) : ModeSelectionAction

  // Validation flow
  data object Submit : ModeSelectionAction

  data object ValidationSucceeded : ModeSelectionAction

  data object ValidationFailed : ModeSelectionAction

  // Mode selection (for offline/demo)
  data class SelectMode(
    val mode: AppMode,
  ) : ModeSelectionAction
}
