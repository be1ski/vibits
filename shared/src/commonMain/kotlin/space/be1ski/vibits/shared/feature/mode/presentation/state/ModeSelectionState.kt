package space.be1ski.vibits.shared.feature.mode.presentation.state

data class ModeSelectionState(
  val showCredentialsDialog: Boolean = false,
  val showQuickOnlineDialog: Boolean = false,
  val hasStoredCredentials: Boolean = false,
  val baseUrl: String = "",
  val token: String = "",
  val isValidating: Boolean = false,
  val error: ModeSelectionError? = null,
)

enum class ModeSelectionError {
  FILL_ALL_FIELDS,
  CONNECTION_FAILED,
}
