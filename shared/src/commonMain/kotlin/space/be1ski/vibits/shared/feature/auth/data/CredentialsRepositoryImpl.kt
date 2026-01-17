package space.be1ski.vibits.shared.feature.auth.data

import dev.zacsweers.metro.Inject
import space.be1ski.vibits.shared.core.logging.Log
import space.be1ski.vibits.shared.feature.auth.domain.model.Credentials
import space.be1ski.vibits.shared.feature.auth.domain.repository.CredentialsRepository

private const val TAG = "Credentials"
private const val URL_LOG_MAX_LENGTH = 50

/**
 * Repository implementation backed by platform credential storage.
 */
@Inject
class CredentialsRepositoryImpl(
  private val credentialsStore: CredentialsStore,
) : CredentialsRepository {
  /**
   * Loads stored credentials from the platform store.
   */
  override fun load(): Credentials {
    val local = credentialsStore.load()
    val maskedUrl = maskUrl(local.baseUrl)
    val hasToken = local.token.isNotBlank()
    Log.i(TAG, "load() baseUrl='$maskedUrl' hasToken=$hasToken")
    return Credentials(baseUrl = local.baseUrl, token = local.token)
  }

  /**
   * Persists credentials to the platform store.
   */
  override fun save(credentials: Credentials) {
    val maskedUrl = maskUrl(credentials.baseUrl)
    Log.i(TAG, "save() baseUrl='$maskedUrl'")
    credentialsStore.save(LocalCredentials(baseUrl = credentials.baseUrl, token = credentials.token))
  }

  private fun maskUrl(url: String): String = url.take(URL_LOG_MAX_LENGTH) + if (url.length > URL_LOG_MAX_LENGTH) "..." else ""
}
