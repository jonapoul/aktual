package aktual.api.client

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class, binding<TokenExpiredEvent>())
@ContributesBinding(AppScope::class, binding<TokenExpiredNotifier>())
class TokenExpiredManager : TokenExpiredEvent, TokenExpiredNotifier {
  private val mutableTokenExpired = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
  override val event: Flow<Unit> = mutableTokenExpired.asSharedFlow()

  override fun notifyExpired() {
    mutableTokenExpired.tryEmit(Unit)
  }
}
