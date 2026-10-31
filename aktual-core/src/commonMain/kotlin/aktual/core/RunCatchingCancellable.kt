package aktual.core

import kotlin.coroutines.cancellation.CancellationException

// Like runCatching, but rethrows CancellationException so coroutine cancellation still propagates
inline fun <T> runCatchingCancellable(block: () -> T): Result<T> =
  try {
    Result.success(block())
  } catch (e: CancellationException) {
    throw e
  } catch (e: Exception) {
    Result.failure(e)
  }
