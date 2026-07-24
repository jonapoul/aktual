package aktual.core.model

import alakazam.kotlin.StateHolder

enum class PingState {
  Unknown,
  Failure,
  Success,
}

class PingStateHolder : StateHolder<PingState>(initialState = PingState.Unknown)
