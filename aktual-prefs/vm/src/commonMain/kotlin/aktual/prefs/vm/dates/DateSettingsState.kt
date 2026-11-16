package aktual.prefs.vm.dates

import aktual.budget.model.DateFormat
import aktual.budget.model.FirstDayOfWeek
import aktual.prefs.vm.ListPreference
import androidx.compose.runtime.Immutable
import kotlinx.datetime.LocalDate

@Immutable
data class DateSettingsState(
  val dateFormat: ListPreference<DateFormat>,
  val firstDayOfWeek: ListPreference<FirstDayOfWeek>,
  val today: LocalDate,
)
