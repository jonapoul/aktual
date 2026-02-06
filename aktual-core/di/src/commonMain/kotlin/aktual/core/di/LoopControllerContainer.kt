package aktual.core.di

import alakazam.kotlin.InfiniteLoopController
import alakazam.kotlin.LoopController
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.ContributesTo

@BindingContainer
@ContributesTo(AppScope::class)
interface LoopControllerContainer {
  @Binds
  val InfiniteLoopController.loopController: LoopController
}
