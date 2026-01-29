package space.be1ski.vibits.shared.feature.memos.presentation

/**
 * Side effects for the Memos feature.
 */
sealed interface MemosEffect {
  sealed interface Credentials : MemosEffect

  sealed interface Load : MemosEffect

  sealed interface Write : MemosEffect

  data object LoadCredentials : Credentials

  data class SaveCredentials(
    val baseUrl: String,
    val token: String,
  ) : Credentials

  data object LoadCachedMemos : Load

  data object LoadRemoteMemos : Load

  data class CreateMemo(
    val content: String,
  ) : Write

  data class UpdateMemo(
    val name: String,
    val content: String,
  ) : Write

  data class DeleteMemo(
    val name: String,
  ) : Write
}
