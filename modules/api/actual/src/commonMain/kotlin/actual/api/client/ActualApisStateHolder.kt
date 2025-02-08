package actual.api.client

import alakazam.kotlin.core.StateHolder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActualApisStateHolder @Inject constructor() : StateHolder<ActualApis?>(initialState = null)
