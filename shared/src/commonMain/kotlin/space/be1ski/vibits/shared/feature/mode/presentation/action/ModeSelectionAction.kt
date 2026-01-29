package space.be1ski.vibits.shared.feature.mode.presentation.action

import space.be1ski.vibits.shared.core.elm.Action
import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode

sealed interface ModeSelectionAction : Action {
  // Stored credentials check
  data object StoredCredentialsFound : ModeSelectionAction

  data object StoredCredentialsNotFound : ModeSelectionAction

  // Quick online dialog
  data object DismissQuickOnlineDialog : ModeSelectionAction

  data object UseStoredCredentials : ModeSelectionAction

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
