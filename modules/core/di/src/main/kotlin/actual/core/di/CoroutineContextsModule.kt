package actual.core.di

import actual.core.coroutines.CoroutineContexts
import actual.core.coroutines.DefaultCoroutineContexts
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface CoroutineContextsModule {
  @Binds
  @Singleton
  fun contexts(impl: DefaultCoroutineContexts): CoroutineContexts
}
