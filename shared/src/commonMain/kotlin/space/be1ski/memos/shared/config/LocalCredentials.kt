package space.be1ski.memos.shared.config

data class LocalCredentials(
  val baseUrl: String,
  val token: String
)

expect class CredentialsStore() {
  fun load(): LocalCredentials
  fun save(credentials: LocalCredentials)
}
