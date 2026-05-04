package aktual.di

import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides

@BindingContainer
@ContributesTo(AppScope::class)
object RunLevelBindingContainer {
  @Provides internal fun state(holder: RunLevelHolder): RunLevelState = holder

  @Provides internal fun controller(holder: RunLevelHolder): RunLevelController = holder
}
