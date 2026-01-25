package space.be1ski.vibits.shared.feature.memos.domain.model

/**
 * Domain model for Memos feature containing business content and logic.
 */
data class MemosContent(
  val memos: List<Memo> = emptyList(),
  val baseUrl: String = "",
  val token: String = "",
  val isOfflineMode: Boolean = false,
  val activePostFilter: PostFilter = PostFilter.ALL,
) {
  val hasCredentials: Boolean get() = baseUrl.isNotBlank() && token.isNotBlank()
  val needsCredentials: Boolean get() = !isOfflineMode && !hasCredentials
}
