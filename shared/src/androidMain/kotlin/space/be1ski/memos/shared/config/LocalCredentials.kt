package space.be1ski.memos.shared.config

actual fun loadLocalCredentials(): LocalCredentials {
  return LocalCredentials(baseUrl = "https://memos.int.be1ski.space", token = "REDACTED_TOKEN")
}
