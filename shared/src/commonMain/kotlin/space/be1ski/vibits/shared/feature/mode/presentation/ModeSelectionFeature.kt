package space.be1ski.vibits.shared.feature.mode.presentation

import space.be1ski.vibits.shared.feature.mode.domain.model.AppMode

sealed interface ModeSelectionAction {
  // Dialog lifecycle
  data object ShowCredentialsDialog : ModeSelectionAction
  data object DismissCredentialsDialog : ModeSelectionAction

  // Credentials input
  data class UpdateBaseUrl(val value: String) : ModeSelectionAction
  data class UpdateToken(val value: String) : ModeSelectionAction

  // Validation flow
  data object Submit : ModeSelectionAction
  data object ValidationSucceeded : ModeSelectionAction
  data object ValidationFailed : ModeSelectionAction

  // Mode selection (for offline/demo)
  data class SelectMode(val mode: AppMode) : ModeSelectionAction
}

data class ModeSelectionState(
  val showCredentialsDialog: Boolean = false,
  val baseUrl: String = "",
  val token: String = "",
  val isValidating: Boolean = false,
  val error: ModeSelectionError? = null,
)

enum class ModeSelectionError {
  FILL_ALL_FIELDS,
  CONNECTION_FAILED,
}

sealed interface ModeSelectionEffect {
  // Async operations (handled by EffectHandler)
  data class ValidateCredentials(
    val baseUrl: String,
    val token: String,
  ) : ModeSelectionEffect

  data class SaveCredentials(
    val baseUrl: String,
    val token: String,
  ) : ModeSelectionEffect

  data class SaveMode(val mode: AppMode) : ModeSelectionEffect

  // Parent notifications (observed by AppRoot)
  data class NotifyModeSelected(val mode: AppMode) : ModeSelectionEffect
}
