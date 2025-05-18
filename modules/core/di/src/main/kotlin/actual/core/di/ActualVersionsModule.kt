package actual.core.di

import actual.core.model.ActualVersionsStateHolder
import alakazam.kotlin.core.BuildConfig
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ActualVersionsModule {
  @Provides
  @Singleton
  fun holder(buildConfig: BuildConfig) = ActualVersionsStateHolder(buildConfig)
}
