package aktual.core

import aktual.di.AppScope
import alakazam.kotlin.TimeZoneProvider
import dev.zacsweers.metro.ContributesBinding
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.todayIn

fun interface Calendar {
  fun today(): LocalDate

  // Polls rather than sleeping until midnight, so clock and time zone changes are picked up too
  fun observeToday(): Flow<LocalDate> = flow {
    while (true) {
      emit(today())
      delay(POLL_INTERVAL)
    }
  }
    .distinctUntilChanged()

  @ContributesBinding(AppScope::class)
  class System(private val clock: Clock, private val timeZones: TimeZoneProvider) : Calendar {
    override fun today(): LocalDate = clock.todayIn(timeZones.get())
  }
}

private val POLL_INTERVAL = 1.minutes
