package space.be1ski.memos.shared.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import space.be1ski.memos.shared.domain.model.Credentials
import space.be1ski.memos.shared.domain.usecase.CreateMemoUseCase
import space.be1ski.memos.shared.domain.usecase.DeleteMemoUseCase
import space.be1ski.memos.shared.domain.usecase.LoadCredentialsUseCase
import space.be1ski.memos.shared.domain.usecase.LoadMemosUseCase
import space.be1ski.memos.shared.domain.usecase.SaveCredentialsUseCase
import space.be1ski.memos.shared.domain.usecase.UpdateMemoUseCase
import space.be1ski.memos.shared.presentation.state.MemosUiState

/**
 * ViewModel that drives memo loading and UI state updates.
 */
class MemosViewModel(
  private val loadMemosUseCase: LoadMemosUseCase,
  private val loadCredentialsUseCase: LoadCredentialsUseCase,
  private val saveCredentialsUseCase: SaveCredentialsUseCase,
  private val updateMemoUseCase: UpdateMemoUseCase,
  private val createMemoUseCase: CreateMemoUseCase,
  private val deleteMemoUseCase: DeleteMemoUseCase
) {
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

  /**
   * Snapshot of UI state for Compose.
   */
  var uiState by mutableStateOf(
    initialState(loadCredentialsUseCase)
  )
    private set

  /**
   * Updates the base URL input.
   */
  fun updateBaseUrl(value: String) {
    val state = uiState
    if (state is MemosUiState.CredentialsInput) {
      uiState = state.copy(baseUrl = value, errorMessage = null)
    }
  }

  /**
   * Updates the token input.
   */
  fun updateToken(value: String) {
    val state = uiState
    if (state is MemosUiState.CredentialsInput) {
      uiState = state.copy(token = value, errorMessage = null)
    }
  }

  /**
   * Opens credentials editor with stored values.
   */
  fun editCredentials() {
    val memos = uiState.memos
    val creds = loadCredentialsUseCase()
    uiState = MemosUiState.CredentialsInput(
      baseUrl = creds.baseUrl,
      token = creds.token,
      memos = memos
    )
  }

  /**
   * Loads memos and persists credentials on success.
   */
  fun loadMemos() {
    val currentState = uiState
    val baseUrl = if (currentState is MemosUiState.CredentialsInput) {
      currentState.baseUrl.trim()
    } else {
      ""
    }
    val token = if (currentState is MemosUiState.CredentialsInput) {
      currentState.token.trim()
    } else {
      ""
    }

    if (currentState is MemosUiState.CredentialsInput) {
      if (baseUrl.isBlank() || token.isBlank()) {
        uiState = currentState.copy(errorMessage = "Base URL and token are required.")
        return
      }
      saveCredentialsUseCase(Credentials(baseUrl = baseUrl, token = token))
      uiState = currentState.copy(isLoading = true, errorMessage = null)
    } else {
      val stored = loadCredentialsUseCase()
      if (stored.baseUrl.isBlank() || stored.token.isBlank()) {
        uiState = MemosUiState.CredentialsInput(
          baseUrl = stored.baseUrl,
          token = stored.token,
          memos = currentState.memos,
          errorMessage = "Base URL and token are required."
        )
        return
      }
      uiState = (currentState as MemosUiState.Ready).copy(isLoading = true, errorMessage = null)
    }

    scope.launch {
      try {
        val memos = loadMemosUseCase()
        uiState = MemosUiState.Ready(memos = memos)
      } catch (error: Exception) {
        val message = error.message ?: "Failed to load memos."
        uiState = when (val state = uiState) {
          is MemosUiState.CredentialsInput -> state.copy(isLoading = false, errorMessage = message)
          is MemosUiState.Ready -> state.copy(isLoading = false, errorMessage = message)
        }
      }
    }
  }

  /**
   * Updates a daily memo content.
   */
  fun updateDailyMemo(name: String, content: String) {
    setLoading(true)
    scope.launch {
      try {
        val updated = updateMemoUseCase(name, content)
        val updatedMemos = uiState.memos.map { memo ->
          if (memo.name == updated.name) updated else memo
        }
        uiState = when (val state = uiState) {
          is MemosUiState.CredentialsInput -> state.copy(isLoading = false, memos = updatedMemos)
          is MemosUiState.Ready -> state.copy(isLoading = false, memos = updatedMemos)
        }
      } catch (error: Exception) {
        val message = error.message ?: "Failed to update memo."
        setLoading(false, message)
      }
    }
  }

  /**
   * Creates a new daily memo.
   */
  fun createDailyMemo(content: String) {
    setLoading(true)
    scope.launch {
      try {
        val created = createMemoUseCase(content)
        val updatedMemos = uiState.memos + created
        uiState = when (val state = uiState) {
          is MemosUiState.CredentialsInput -> state.copy(isLoading = false, memos = updatedMemos)
          is MemosUiState.Ready -> state.copy(isLoading = false, memos = updatedMemos)
        }
      } catch (error: Exception) {
        val message = error.message ?: "Failed to create memo."
        setLoading(false, message)
      }
    }
  }

  /**
   * Deletes a memo by name.
   */
  fun deleteDailyMemo(name: String) {
    setLoading(true)
    scope.launch {
      try {
        deleteMemoUseCase(name)
        val updatedMemos = uiState.memos.filterNot { memo -> memo.name == name }
        uiState = when (val state = uiState) {
          is MemosUiState.CredentialsInput -> state.copy(isLoading = false, memos = updatedMemos)
          is MemosUiState.Ready -> state.copy(isLoading = false, memos = updatedMemos)
        }
      } catch (error: Exception) {
        val message = error.message ?: "Failed to delete memo."
        setLoading(false, message)
      }
    }
  }

  private fun initialState(loadCredentialsUseCase: LoadCredentialsUseCase): MemosUiState {
    val creds = loadCredentialsUseCase()
    return if (creds.baseUrl.isBlank() || creds.token.isBlank()) {
      MemosUiState.CredentialsInput(baseUrl = creds.baseUrl, token = creds.token)
    } else {
      MemosUiState.Ready()
    }
  }

  private fun setLoading(isLoading: Boolean, errorMessage: String? = null) {
    uiState = when (val state = uiState) {
      is MemosUiState.CredentialsInput -> state.copy(isLoading = isLoading, errorMessage = errorMessage)
      is MemosUiState.Ready -> state.copy(isLoading = isLoading, errorMessage = errorMessage)
    }
  }
}
