package space.be1ski.vibits.feature.auth.data

import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import space.be1ski.vibits.core.logging.Log
import space.be1ski.vibits.core.logging.maskUrl
import space.be1ski.vibits.core.platform.di.AppScope
import space.be1ski.vibits.feature.auth.data.platform.CredentialsStore
import space.be1ski.vibits.feature.auth.domain.model.Credentials
import space.be1ski.vibits.feature.auth.domain.model.trimmed
import space.be1ski.vibits.feature.auth.domain.repository.CredentialsRepository

private const val TAG = "Credentials"

@Inject
@SingleIn(AppScope::class)
class CredentialsRepositoryImpl(
  private val credentialsStore: CredentialsStore,
) : CredentialsRepository {
  override fun load(): Credentials {
    val local = credentialsStore.load()
    val hasToken = local.token.isNotBlank()
    Log.i(TAG, "load() baseUrl='${local.baseUrl.maskUrl()}' hasToken=$hasToken")
    return Credentials(baseUrl = local.baseUrl, token = local.token)
  }

  override fun save(credentials: Credentials) {
    val trimmed = credentials.trimmed()
    Log.i(TAG, "save() baseUrl='${trimmed.baseUrl.maskUrl()}'")
    credentialsStore.save(LocalCredentials(baseUrl = trimmed.baseUrl, token = trimmed.token))
  }
}
