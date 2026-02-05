package space.be1ski.vibits.feature.auth.domain.usecase

import dev.zacsweers.metro.Inject
import space.be1ski.vibits.core.platform.env.LocalConfigProvider
import space.be1ski.vibits.core.utils.logging.Log
import space.be1ski.vibits.core.utils.logging.maskUrl
import space.be1ski.vibits.feature.auth.domain.model.Credentials

private const val TAG = "InitCredentialsFromConfig"
private const val CONFIG_MEMOS_BASE_URL = "memos.baseUrl"
private const val CONFIG_MEMOS_TOKEN = "memos.token"

/**
 * Initializes credentials from local configuration on first launch.
 * Checks if credentials are already saved. If not, attempts to load from local.properties.
 */
@Inject
class InitializeCredentialsFromEnvUseCase(
  private val loadCredentials: LoadCredentialsUseCase,
  private val saveCredentials: SaveCredentialsUseCase,
  private val localConfigProvider: LocalConfigProvider,
) {
  operator fun invoke() {
    val existing = loadCredentials()
    if (existing.baseUrl.isNotBlank() || existing.token.isNotBlank()) {
      Log.i(TAG, "Credentials already saved, skipping initialization")
      return
    }

    val baseUrl = localConfigProvider.get(CONFIG_MEMOS_BASE_URL)
    val token = localConfigProvider.get(CONFIG_MEMOS_TOKEN)

    val maskedUrl = baseUrl?.maskUrl() ?: "null"
    val maskedToken = if (token.isNullOrBlank()) "null" else "***"
    Log.i(TAG, "Checking config: baseUrl=$maskedUrl token=$maskedToken")

    if (baseUrl.isNullOrBlank() || token.isNullOrBlank()) {
      Log.i(TAG, "Config not set, skipping initialization")
      return
    }

    Log.i(TAG, "Initializing credentials from local config")
    saveCredentials(Credentials(baseUrl = baseUrl, token = token))
  }
}
