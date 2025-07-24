package actual.core.model

import alakazam.kotlin.core.StateHolder
import javax.inject.Inject
import javax.inject.Singleton

enum class PingState {
  Unknown,
  Failure,
  Success,
}

@Singleton
class PingStateHolder @Inject constructor() : StateHolder<PingState>(initialState = PingState.Unknown)
