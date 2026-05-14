package aktual.core.model

import aktual.di.AppScope
import alakazam.kotlin.TimeZoneProvider
import dev.zacsweers.metro.ContributesBinding
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.todayIn

fun interface Calendar {
  fun today(): LocalDate

  @ContributesBinding(AppScope::class)
  class System(private val clock: Clock, private val timeZones: TimeZoneProvider) : Calendar {
    override fun today(): LocalDate = clock.todayIn(timeZones.get())
  }
}
