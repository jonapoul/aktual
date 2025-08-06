package actual.di.app

import alakazam.kotlin.time.TimeZoneProvider
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import kotlin.time.Clock

@BindingContainer
@ContributesTo(AppScope::class)
object ClockContainer {
  @Provides
  fun clock(): Clock = Clock.System

  @Provides
  fun timeZones(): TimeZoneProvider = TimeZoneProvider.Default
}
