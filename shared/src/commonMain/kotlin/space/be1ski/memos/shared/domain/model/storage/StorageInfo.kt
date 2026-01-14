package space.be1ski.memos.shared.domain.model.storage

/**
 * Human-readable storage information for support/debugging.
 */
data class StorageInfo(
  val environment: String,
  val credentialsStore: String,
  val memosDatabase: String,
  val offlineStorage: String
)
