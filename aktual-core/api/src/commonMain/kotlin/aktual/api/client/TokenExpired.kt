package aktual.api.client

import kotlinx.coroutines.flow.Flow

interface TokenExpiredEvent {
  val event: Flow<Unit>
}

fun interface TokenExpiredNotifier {
  fun notifyExpired()
}
