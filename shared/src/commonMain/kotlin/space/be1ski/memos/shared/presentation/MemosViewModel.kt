package space.be1ski.memos.shared.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import space.be1ski.memos.shared.domain.model.Credentials
import space.be1ski.memos.shared.domain.usecase.LoadCredentialsUseCase
import space.be1ski.memos.shared.domain.usecase.LoadMemosUseCase
import space.be1ski.memos.shared.domain.usecase.SaveCredentialsUseCase
import space.be1ski.memos.shared.presentation.state.MemosUiState

/**
 * ViewModel that drives memo loading and UI state updates.
 */
class MemosViewModel(
  private val loadMemosUseCase: LoadMemosUseCase,
  private val loadCredentialsUseCase: LoadCredentialsUseCase,
  private val saveCredentialsUseCase: SaveCredentialsUseCase
) {
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

  /**
   * Snapshot of UI state for Compose.
   */
  var uiState by mutableStateOf(
    loadCredentialsUseCase().let { creds ->
      MemosUiState(baseUrl = creds.baseUrl, token = creds.token)
    }
  )
    private set

  /**
   * Updates the base URL input.
   */
  fun updateBaseUrl(value: String) {
    uiState = uiState.copy(baseUrl = value, errorMessage = null)
  }

  /**
   * Updates the token input.
   */
  fun updateToken(value: String) {
    uiState = uiState.copy(token = value, errorMessage = null)
  }

  /**
   * Loads memos and persists credentials on success.
   */
  fun loadMemos() {
    val baseUrl = uiState.baseUrl.trim()
    val token = uiState.token.trim()
    if (baseUrl.isBlank() || token.isBlank()) {
      uiState = uiState.copy(errorMessage = "Base URL and token are required.")
      return
    }

    uiState = uiState.copy(isLoading = true, errorMessage = null)
    scope.launch {
      try {
        val memos = loadMemosUseCase(baseUrl, token)
        saveCredentialsUseCase(Credentials(baseUrl = baseUrl, token = token))
        uiState = uiState.copy(isLoading = false, memos = memos)
      } catch (error: Exception) {
        uiState = uiState.copy(
          isLoading = false,
          errorMessage = error.message ?: "Failed to load memos."
        )
      }
    }
  }
}
