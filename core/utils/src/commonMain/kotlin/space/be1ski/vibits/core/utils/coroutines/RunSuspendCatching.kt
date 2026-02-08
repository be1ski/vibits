package space.be1ski.vibits.core.utils.coroutines

import kotlinx.coroutines.CancellationException

/**
 * Like [runCatching], but rethrows [CancellationException] instead of wrapping it in [Result.failure].
 *
 * In suspend contexts, catching [CancellationException] breaks structured concurrency —
 * the coroutine can no longer be cancelled. Always use this instead of [runCatching] in suspend code.
 */
@Suppress("TooGenericExceptionCaught")
suspend inline fun <R> runSuspendCatching(block: () -> R): Result<R> =
  try {
    Result.success(block())
  } catch (ce: CancellationException) {
    throw ce
  } catch (e: Throwable) {
    Result.failure(e)
  }

/**
 * Like [runCatching], but rethrows [CancellationException] instead of wrapping it in [Result.failure].
 *
 * Extension variant for calling methods on a receiver.
 */
@Suppress("TooGenericExceptionCaught")
suspend inline fun <T, R> T.runSuspendCatching(block: T.() -> R): Result<R> =
  try {
    Result.success(block())
  } catch (ce: CancellationException) {
    throw ce
  } catch (e: Throwable) {
    Result.failure(e)
  }
