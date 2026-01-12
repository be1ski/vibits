package space.be1ski.memos.shared.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import space.be1ski.memos.shared.config.CredentialsStore
import space.be1ski.memos.shared.config.LocalCredentials
import space.be1ski.memos.shared.data.MemosRepository
import space.be1ski.memos.shared.ui.state.MemosUiState

/**
 * ViewModel that drives memo loading and UI state updates.
 */
class MemosViewModel(
  private val repository: MemosRepository,
  private val credentialsStore: CredentialsStore
) {
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

  /**
   * Snapshot of UI state for Compose.
   */
  var uiState by mutableStateOf(
    credentialsStore.load().let { creds ->
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
        val memos = repository.listMemos(baseUrl, token)
        credentialsStore.save(LocalCredentials(baseUrl = baseUrl, token = token))
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
