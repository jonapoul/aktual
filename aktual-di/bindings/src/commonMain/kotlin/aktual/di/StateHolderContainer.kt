package aktual.di

import aktual.core.model.AktualVersionsStateHolder
import aktual.core.model.BuildConfig
import aktual.core.model.PingStateHolder
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@BindingContainer
@ContributesTo(AppScope::class)
object StateHolderContainer {
  @Provides @SingleIn(AppScope::class) fun pingState(): PingStateHolder = PingStateHolder()

  @Provides
  @SingleIn(AppScope::class)
  fun versions(config: BuildConfig): AktualVersionsStateHolder = AktualVersionsStateHolder(config)
}
