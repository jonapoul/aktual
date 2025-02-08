package actual.core.di

import actual.log.Logger
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface LoggerModule {
  @Binds
  @Singleton
  fun logger(impl: ActualLogger): Logger
}
