package space.be1ski.memos.shared.presentation.state

import space.be1ski.memos.shared.domain.model.Memo

/**
 * UI state for the memos screen.
 */
sealed class MemosUiState {
  /**
   * Loaded memos.
   */
  abstract val memos: List<Memo>

  /**
   * True while data is loading.
   */
  abstract val isLoading: Boolean

  /**
   * User-facing error message, if any.
   */
  abstract val errorMessage: String?

  /**
   * State used when credentials are missing or being edited.
   */
  data class CredentialsInput(
    val baseUrl: String = "",
    val token: String = "",
    override val memos: List<Memo> = emptyList(),
    override val isLoading: Boolean = false,
    override val errorMessage: String? = null
  ) : MemosUiState()

  /**
   * State used when credentials are already stored.
   */
  data class Ready(
    override val memos: List<Memo> = emptyList(),
    override val isLoading: Boolean = false,
    override val errorMessage: String? = null
  ) : MemosUiState()
}
