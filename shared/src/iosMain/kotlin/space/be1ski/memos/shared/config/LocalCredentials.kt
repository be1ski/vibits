package space.be1ski.memos.shared.config

actual fun loadLocalCredentials(): LocalCredentials {
  return LocalCredentials(baseUrl = "", token = "")
}
