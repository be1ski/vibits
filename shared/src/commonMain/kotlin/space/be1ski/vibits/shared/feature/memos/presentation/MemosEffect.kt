package space.be1ski.vibits.shared.feature.memos.presentation

/**
 * Side effects for the Memos feature.
 */
sealed interface MemosEffect {
  data object LoadCachedMemos : MemosEffect

  data object LoadRemoteMemos : MemosEffect

  data class SaveCredentials(
    val baseUrl: String,
    val token: String,
  ) : MemosEffect

  data object LoadCredentials : MemosEffect

  data class CreateMemo(
    val content: String,
  ) : MemosEffect

  data class UpdateMemo(
    val name: String,
    val content: String,
  ) : MemosEffect

  data class DeleteMemo(
    val name: String,
  ) : MemosEffect
}
