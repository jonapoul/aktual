package aktual.core.di

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO

@BindingContainer
@ContributesTo(AppScope::class)
object HttpContainer {
  @Provides @SingleIn(AppScope::class) fun engine(): HttpClientEngine = CIO.create()
}
