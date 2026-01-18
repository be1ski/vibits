package space.be1ski.vibits.shared.feature.memos.data

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import space.be1ski.vibits.shared.app.di.AppScope
import space.be1ski.vibits.shared.core.logging.Log
import space.be1ski.vibits.shared.feature.memos.data.remote.MemosApi

fun interface ConnectionTester : suspend (String, String) -> Result<Unit>

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
