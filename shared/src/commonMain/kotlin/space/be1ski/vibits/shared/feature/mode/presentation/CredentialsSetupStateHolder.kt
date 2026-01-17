package space.be1ski.vibits.shared.feature.mode.presentation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import space.be1ski.vibits.shared.feature.auth.domain.model.Credentials
import space.be1ski.vibits.shared.feature.auth.domain.usecase.SaveCredentialsUseCase
import space.be1ski.vibits.shared.feature.auth.domain.usecase.ValidateCredentialsUseCase

/**
 * State for the credentials setup dialog.
 */
data class CredentialsSetupState(
  val baseUrl: String = "",
  val token: String = "",
  val isLoading: Boolean = false,
  val errorMessage: String? = null,
)

/**
 * State holder for credentials setup during mode selection.
 * Handles validation and saving of credentials.
 */
class CredentialsSetupStateHolder(
  private val validateCredentialsUseCase: ValidateCredentialsUseCase,
  private val saveCredentialsUseCase: SaveCredentialsUseCase,
) {
  private val _state = MutableStateFlow(CredentialsSetupState())
  val state: StateFlow<CredentialsSetupState> = _state.asStateFlow()

  fun updateBaseUrl(value: String) {
    _state.update { it.copy(baseUrl = value, errorMessage = null) }
  }

  fun updateToken(value: String) {
    _state.update { it.copy(token = value, errorMessage = null) }
  }

  fun setError(message: String) {
    _state.update { it.copy(errorMessage = message) }
  }

  fun reset() {
    _state.value = CredentialsSetupState()
  }

  /**
   * Validates and saves credentials.
   * @return true if successful, false otherwise
   */
  suspend fun validateAndSave(): Boolean {
    val currentState = _state.value
    val baseUrl = currentState.baseUrl.trim()
    val token = currentState.token.trim()

    _state.update { it.copy(isLoading = true, errorMessage = null) }

    val result = validateCredentialsUseCase(baseUrl, token)

    return result.fold(
      onSuccess = {
        saveCredentialsUseCase(Credentials(baseUrl, token))
        _state.update { it.copy(isLoading = false) }
        true
      },
      onFailure = { e ->
        _state.update {
          it.copy(
            isLoading = false,
            errorMessage = e.message,
          )
        }
        false
      },
    )
  }
}
