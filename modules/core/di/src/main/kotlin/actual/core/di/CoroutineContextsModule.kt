package actual.core.di

import alakazam.kotlin.core.CoroutineContexts
import alakazam.kotlin.core.DefaultCoroutineContexts
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class CoroutineContextsModule {
  @Provides
  @Singleton
  fun contexts(): CoroutineContexts = DefaultCoroutineContexts()
}
