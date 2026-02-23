package aktual.api.client

import alakazam.kotlin.StateHolder
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(AppScope::class)
class AktualApisStateHolder : StateHolder<AktualApis?>(initialState = null)
