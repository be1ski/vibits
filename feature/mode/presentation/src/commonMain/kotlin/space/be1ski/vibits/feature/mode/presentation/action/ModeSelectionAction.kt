package space.be1ski.vibits.feature.mode.presentation.action

import space.be1ski.vibits.core.elm.Action
import space.be1ski.vibits.core.platform.mode.AppMode

sealed interface ModeSelectionAction : Action {
  /** Stored credentials detection actions. */
  sealed interface StoredCredentials : ModeSelectionAction {
    data object Found : StoredCredentials

    data object NotFound : StoredCredentials
  }

  /** Quick online dialog actions. */
  sealed interface QuickOnline : ModeSelectionAction {
    data object Dismiss : QuickOnline

    data object UseStoredCredentials : QuickOnline
  }

  /** Credentials dialog lifecycle actions. */
  sealed interface Dialog : ModeSelectionAction {
    data object Show : Dialog

    data object Dismiss : Dialog
  }

  /** Credentials input actions. */
  sealed interface Input : ModeSelectionAction {
    data class UpdateBaseUrl(
      val value: String,
    ) : Input

    data class UpdateToken(
      val value: String,
    ) : Input
  }

  /** Validation flow actions. */
  sealed interface Validation : ModeSelectionAction {
    data object Submit : Validation

    data object Succeeded : Validation

    data object Failed : Validation
  }

  /** Mode selection actions. */
  sealed interface Selection : ModeSelectionAction {
    data class SelectMode(
      val mode: AppMode,
    ) : Selection
  }
}
