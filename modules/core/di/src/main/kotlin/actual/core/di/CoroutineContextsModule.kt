package actual.core.di

import alakazam.kotlin.core.CoroutineContexts
import alakazam.kotlin.core.DefaultCoroutineContexts
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class CoroutineContextsModule {
  @Provides
  @Singleton
  fun contexts(): CoroutineContexts = DefaultCoroutineContexts()
}
