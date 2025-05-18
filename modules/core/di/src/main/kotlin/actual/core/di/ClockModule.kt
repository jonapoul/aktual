package actual.core.di

import dagger.Module
import dagger.Provides
import kotlinx.datetime.Clock
import javax.inject.Singleton

@Module
class ClockModule {
  @Provides
  @Singleton
  fun clock(): Clock = Clock.System
}
