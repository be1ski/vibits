package space.be1ski.vibits.feature.memos.data

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import space.be1ski.vibits.core.logging.Log
import space.be1ski.vibits.core.platform.di.AppScope
import space.be1ski.vibits.feature.memos.data.remote.MemosApi
import space.be1ski.vibits.feature.memos.domain.repository.ConnectionTester

private const val TAG = "ConnectionTester"

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class ConnectionTesterImpl(
  private val memosApi: MemosApi,
) : ConnectionTester {
  override suspend operator fun invoke(
    baseUrl: String,
    token: String,
  ): Result<Unit> {
    Log.d(TAG, "Testing connection...")
    return runCatching {
      memosApi.listMemos(
        baseUrl = baseUrl,
        token = token,
        pageSize = 1,
        pageToken = null,
      )
    }.onSuccess {
      Log.i(TAG, "Connection successful")
    }.onFailure {
      Log.w(TAG, "Connection failed: ${it.message}")
    }.map { }
  }
}
