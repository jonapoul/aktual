package actual.core.di

import actual.core.model.TimeZones
import dagger.Module
import dagger.Provides
import kotlinx.datetime.Clock
import javax.inject.Singleton

@Module
class ClockModule {
  @Provides
  @Singleton
  fun clock(): Clock = Clock.System

  @Provides
  fun timeZones(): TimeZones = TimeZones.System
}
