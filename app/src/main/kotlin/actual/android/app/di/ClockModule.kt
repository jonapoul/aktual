package actual.android.app.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.datetime.Clock
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class ClockModule {
  @Provides
  @Singleton
  fun clock(): Clock = Clock.System
}
