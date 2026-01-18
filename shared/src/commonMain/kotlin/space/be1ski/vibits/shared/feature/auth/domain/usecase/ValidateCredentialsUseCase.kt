package space.be1ski.vibits.shared.feature.auth.domain.usecase

import dev.zacsweers.metro.Inject
import space.be1ski.vibits.shared.core.logging.Log
import space.be1ski.vibits.shared.feature.memos.data.remote.MemosApi

private const val TAG = "ValidateCreds"

fun interface ValidateCredentials {
  suspend operator fun invoke(
    baseUrl: String,
    token: String,
  ): Result<Unit>
}

/**
 * Use case that validates credentials by making a test API request.
 * Returns true if credentials are valid, false otherwise.
 */
@Inject
class ValidateCredentialsUseCase(
  private val memosApi: MemosApi,
) : ValidateCredentials {
  override suspend operator fun invoke(
    baseUrl: String,
    token: String,
  ): Result<Unit> {
    Log.d(TAG, "Validating credentials...")
    return runCatching {
      memosApi.listMemos(
        baseUrl = baseUrl,
        token = token,
        pageSize = 1,
        pageToken = null,
      )
    }.onSuccess {
      Log.i(TAG, "Credentials valid")
    }.onFailure {
      Log.w(TAG, "Credentials invalid: ${it.message}")
    }.map { }
  }
}
