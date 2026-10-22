package aktual.app.android

import aktual.di.AppScope
import aktual.prefs.AppLockAuthenticator
import android.content.Context
import androidx.compose.runtime.Stable
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

// Only the activity can show the prompt, so requests are passed to it and it reports back
@Inject
@Stable
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class AndroidAppLockAuthenticator(private val context: Context) : AppLockAuthenticator {
  private val mutableRequests = Channel<Unit>(capacity = Channel.CONFLATED)
  internal val requests: Flow<Unit> = mutableRequests.receiveAsFlow()

  private var pending: CompletableDeferred<Boolean>? = null

  override fun isAvailable(): Boolean = BiometricAuthenticator.canAuthenticate(context) == Available

  override suspend fun authenticate(): Boolean {
    val result = CompletableDeferred<Boolean>()
    pending?.complete(false)
    pending = result
    mutableRequests.send(Unit)
    return try {
      result.await()
    } finally {
      if (pending === result) pending = null
    }
  }

  // Returns false if nothing was waiting on the result
  internal fun onResult(success: Boolean): Boolean {
    val result = pending ?: return false
    pending = null
    result.complete(success)
    return true
  }
}
