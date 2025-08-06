package actual.di.app

import alakazam.kotlin.core.CoroutineContexts
import alakazam.kotlin.core.DefaultCoroutineContexts
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

@BindingContainer
@ContributesTo(AppScope::class)
object CoroutineContainer {
  @Provides
  @SingleIn(AppScope::class)
  fun contexts(): CoroutineContexts = DefaultCoroutineContexts()

  @Provides
  @SingleIn(AppScope::class)
  fun scope(): CoroutineScope = CoroutineScope(SupervisorJob())
}
