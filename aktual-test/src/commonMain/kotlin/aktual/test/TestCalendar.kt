package aktual.test

import aktual.core.Calendar
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate

// The real calendar polls forever, which a test scheduler would never finish skipping through
class TestCalendar(today: LocalDate) : Calendar {
  private val date = MutableStateFlow(today)

  override fun today(): LocalDate = date.value

  override fun observeToday(): Flow<LocalDate> = date

  fun set(today: LocalDate) = date.update { today }
}
