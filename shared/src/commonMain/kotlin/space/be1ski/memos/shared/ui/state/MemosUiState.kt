package space.be1ski.memos.shared.ui.state

import space.be1ski.memos.shared.model.Memo

/**
 * Immutable UI state for the Memos screen.
 */
data class MemosUiState(
  /**
   * Base URL for the Memos server.
   */
  val baseUrl: String = "",
  /**
   * Access token for API requests.
   */
  val token: String = "",
  /**
   * True while data is loading.
   */
  val isLoading: Boolean = false,
  /**
   * User-facing error message, if any.
   */
  val errorMessage: String? = null,
  /**
   * Loaded memos.
   */
  val memos: List<Memo> = emptyList()
)
