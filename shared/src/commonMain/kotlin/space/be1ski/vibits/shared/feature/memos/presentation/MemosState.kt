package space.be1ski.vibits.shared.feature.memos.presentation

import space.be1ski.vibits.shared.feature.memos.domain.model.Memo

/**
 * State for the Memos feature.
 */
data class MemosState(
  val memos: List<Memo> = emptyList(),
  val isLoading: Boolean = false,
  val errorMessage: String? = null,
  val credentialsMode: Boolean = false,
  val baseUrl: String = "",
  val token: String = "",
  val isOfflineMode: Boolean = false,
) {
  val hasCredentials: Boolean get() = baseUrl.isNotBlank() && token.isNotBlank()
  val needsCredentials: Boolean get() = !isOfflineMode && !hasCredentials
}
