package aktual.core

import kotlin.coroutines.cancellation.CancellationException

// Like runCatching, but rethrows CancellationException so coroutine cancellation still propagates
inline fun <T> runCatchingCancellable(
  attempt: () -> T,
  onFailure: (Exception) -> T,
  finally: () -> Unit = {},
): T =
  try {
    attempt()
  } catch (e: CancellationException) {
    throw e
  } catch (e: Exception) {
    onFailure(e)
  } finally {
    finally()
  }
