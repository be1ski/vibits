package space.be1ski.vibits.shared.feature.auth.data

/**
 * In-memory credentials store for web builds.
 */
actual class CredentialsStore {
  private var cached: LocalCredentials? = null

  actual fun load(): LocalCredentials = cached ?: LocalCredentials(baseUrl = "", token = "")

  actual fun save(credentials: LocalCredentials) {
    cached = credentials
  }
}
