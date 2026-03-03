package space.be1ski.vibits.feature.mode.presentation.state

import space.be1ski.vibits.core.ui.form.CredentialValidationError

data class ModeSelectionState(
  val showCredentialsDialog: Boolean = false,
  val showQuickOnlineDialog: Boolean = false,
  val hasStoredCredentials: Boolean = false,
  val isKeychainAvailable: Boolean = false,
  val baseUrl: String = "",
  val token: String = "",
  val isValidating: Boolean = false,
  val error: CredentialValidationError? = null,
)
