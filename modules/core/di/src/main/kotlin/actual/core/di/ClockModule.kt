package actual.core.di

import alakazam.kotlin.time.TimeZoneProvider
import dagger.Module
import dagger.Provides
import javax.inject.Singleton
import kotlin.time.Clock

@Module
class ClockModule {
  @Provides
  @Singleton
  fun clock(): Clock = Clock.System

  @Provides
  fun timeZones(): TimeZoneProvider = TimeZoneProvider.Default
}
