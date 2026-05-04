package aktual.core.model

import aktual.di.AppScope
import alakazam.kotlin.StateHolder
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

enum class PingState {
  Unknown,
  Failure,
  Success,
}

@Inject
@SingleIn(AppScope::class)
class PingStateHolder : StateHolder<PingState>(initialState = PingState.Unknown)
