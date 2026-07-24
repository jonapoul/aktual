package aktual.core

import kotlinx.coroutines.flow.Flow

interface TokenExpiredEvent {
  val event: Flow<Unit>
}

fun interface TokenExpiredNotifier {
  operator fun invoke()
}
