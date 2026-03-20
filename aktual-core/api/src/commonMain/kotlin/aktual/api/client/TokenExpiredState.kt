package aktual.api.client

import kotlinx.coroutines.flow.Flow

interface TokenExpiredState {
  val isExpired: Flow<Unit>
}

fun interface TokenExpiredNotifier {
  fun notifyExpired()
}
