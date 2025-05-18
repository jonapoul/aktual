package actual.core.di

import alakazam.kotlin.core.InfiniteLoopController
import alakazam.kotlin.core.LoopController
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class LoopControllerModule {
  @Provides
  @Singleton
  fun loopController(): LoopController = InfiniteLoopController
}
