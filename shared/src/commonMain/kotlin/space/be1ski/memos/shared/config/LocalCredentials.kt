package space.be1ski.memos.shared.config

data class LocalCredentials(
  val baseUrl: String,
  val token: String
)

expect fun loadLocalCredentials(): LocalCredentials
