package aktual.di

import alakazam.kotlin.CoroutineContexts
import alakazam.kotlin.DefaultCoroutineContexts
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoSet
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

@BindingContainer
@ContributesTo(AppScope::class)
object CoroutineContainer {
  @Provides
  @SingleIn(AppScope::class)
  fun contexts(): CoroutineContexts = DefaultCoroutineContexts()

  @Provides @SingleIn(AppScope::class) fun scope(): CoroutineScope = MainScope()

  @Provides @IntoSet fun closeable(scope: CoroutineScope): Closeable = Closeable { scope.cancel() }
}
