package actual.android.app.di

import actual.core.model.ActualVersionsStateHolder
import alakazam.kotlin.core.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ActualVersionsModule {
  @Provides
  @Singleton
  fun holder(buildConfig: BuildConfig) = ActualVersionsStateHolder(buildConfig)
}
