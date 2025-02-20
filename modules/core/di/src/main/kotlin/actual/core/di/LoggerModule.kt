package actual.core.di

import alakazam.android.core.TimberLogger
import alakazam.kotlin.core.Logger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class LoggerModule {
  @Provides
  @Singleton
  fun logger(): Logger = TimberLogger
}
