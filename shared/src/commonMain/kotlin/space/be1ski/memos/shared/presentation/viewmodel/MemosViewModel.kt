package space.be1ski.memos.shared.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import space.be1ski.memos.shared.domain.model.auth.Credentials
import space.be1ski.memos.shared.domain.model.memo.Memo
import space.be1ski.memos.shared.domain.usecase.CreateMemoUseCase
import space.be1ski.memos.shared.domain.usecase.DeleteMemoUseCase
import space.be1ski.memos.shared.domain.usecase.LoadCachedMemosUseCase
import space.be1ski.memos.shared.domain.usecase.LoadCredentialsUseCase
import space.be1ski.memos.shared.domain.usecase.LoadMemosUseCase
import space.be1ski.memos.shared.domain.usecase.LoadStorageInfoUseCase
import space.be1ski.memos.shared.domain.usecase.SaveCredentialsUseCase
import space.be1ski.memos.shared.domain.usecase.UpdateMemoUseCase
import space.be1ski.memos.shared.presentation.state.MemosUiState

/**
 * ViewModel that drives memo loading and UI state updates.
 */
class MemosViewModel(
  private val loadMemosUseCase: LoadMemosUseCase,
  private val loadCachedMemosUseCase: LoadCachedMemosUseCase,
  private val loadCredentialsUseCase: LoadCredentialsUseCase,
  private val loadStorageInfoUseCase: LoadStorageInfoUseCase,
  private val saveCredentialsUseCase: SaveCredentialsUseCase,
  private val updateMemoUseCase: UpdateMemoUseCase,
  private val createMemoUseCase: CreateMemoUseCase,
  private val deleteMemoUseCase: DeleteMemoUseCase
) {
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

  init {
    preloadCachedMemos()
  }

  /**
   * Snapshot of UI state for Compose.
   */
  var uiState by mutableStateOf(
    initialState(loadCredentialsUseCase)
  )
    private set

  /**
   * Storage details for diagnostics.
   */
  val storageInfo = loadStorageInfoUseCase()

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
      val cached = runCatching { loadCachedMemosUseCase() }.getOrNull().orEmpty()
      if (cached.isNotEmpty() && uiState.memos.isEmpty()) {
        val sortedCached = sortedMemos(cached)
        uiState = when (val state = uiState) {
          is MemosUiState.CredentialsInput -> state.copy(memos = sortedCached, isLoading = true)
          is MemosUiState.Ready -> state.copy(memos = sortedCached, isLoading = true)
        }
      }
      runCatching { loadMemosUseCase() }
        .onSuccess { memos -> uiState = MemosUiState.Ready(memos = sortedMemos(memos)) }
        .onFailure { error ->
          if (error is CancellationException) throw error
          val message = error.message ?: "Failed to load memos."
          uiState = when (val state = uiState) {
            is MemosUiState.CredentialsInput -> state.copy(isLoading = false, errorMessage = message)
            is MemosUiState.Ready -> state.copy(isLoading = false, errorMessage = message)
          }
        }
    }
  }

  /**
   * Updates a memo content.
   */
  fun updateMemo(name: String, content: String) {
    setLoading(true)
    scope.launch {
      runCatching { updateMemoUseCase(name, content) }
        .onSuccess { updated ->
          val updatedMemos = sortedMemos(uiState.memos.map { memo ->
            if (memo.name == updated.name) updated else memo
          })
          uiState = when (val state = uiState) {
            is MemosUiState.CredentialsInput -> state.copy(isLoading = false, memos = updatedMemos)
            is MemosUiState.Ready -> state.copy(isLoading = false, memos = updatedMemos)
          }
        }
        .onFailure { error ->
          if (error is CancellationException) throw error
          val message = error.message ?: "Failed to update memo."
          setLoading(false, message)
        }
    }
  }

  /**
   * Creates a new memo.
   */
  fun createMemo(content: String) {
    setLoading(true)
    scope.launch {
      runCatching { createMemoUseCase(content) }
        .onSuccess { created ->
          val updatedMemos = sortedMemos(uiState.memos + created)
          uiState = when (val state = uiState) {
            is MemosUiState.CredentialsInput -> state.copy(isLoading = false, memos = updatedMemos)
            is MemosUiState.Ready -> state.copy(isLoading = false, memos = updatedMemos)
          }
        }
        .onFailure { error ->
          if (error is CancellationException) throw error
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
      runCatching { deleteMemoUseCase(name) }
        .onSuccess {
          val updatedMemos = sortedMemos(uiState.memos.filterNot { memo -> memo.name == name })
          uiState = when (val state = uiState) {
            is MemosUiState.CredentialsInput -> state.copy(isLoading = false, memos = updatedMemos)
            is MemosUiState.Ready -> state.copy(isLoading = false, memos = updatedMemos)
          }
        }
        .onFailure { error ->
          if (error is CancellationException) throw error
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

  private fun preloadCachedMemos() {
    val creds = loadCredentialsUseCase()
    if (creds.baseUrl.isBlank() || creds.token.isBlank()) {
      return
    }
    scope.launch {
      val cached = runCatching { loadCachedMemosUseCase() }.getOrNull().orEmpty()
      if (cached.isNotEmpty() && uiState.memos.isEmpty()) {
        val sortedCached = sortedMemos(cached)
        uiState = when (val state = uiState) {
          is MemosUiState.CredentialsInput -> state.copy(memos = sortedCached)
          is MemosUiState.Ready -> state.copy(memos = sortedCached)
        }
      }
    }
  }

  private fun sortedMemos(memos: List<Memo>): List<Memo> =
    memos.sortedByDescending(::memoTimestamp)

  private fun memoTimestamp(memo: Memo): Long =
    memo.updateTime?.toEpochMilliseconds()
      ?: memo.createTime?.toEpochMilliseconds()
      ?: Long.MIN_VALUE

  private fun setLoading(isLoading: Boolean, errorMessage: String? = null) {
    uiState = when (val state = uiState) {
      is MemosUiState.CredentialsInput -> state.copy(isLoading = isLoading, errorMessage = errorMessage)
      is MemosUiState.Ready -> state.copy(isLoading = isLoading, errorMessage = errorMessage)
    }
  }
}
