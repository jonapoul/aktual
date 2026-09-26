package aktual.di

import alakazam.kotlin.CoroutineContexts
import alakazam.kotlin.DefaultCoroutineContexts
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@BindingContainer
@ContributesTo(AppScope::class)
object CoroutineContainer {
  @Provides
  @SingleIn(AppScope::class)
  fun contexts(): CoroutineContexts = DefaultCoroutineContexts()
}
