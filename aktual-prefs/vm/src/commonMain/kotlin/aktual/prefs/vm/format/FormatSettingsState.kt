package aktual.prefs.vm.format

import aktual.budget.model.DateFormat
import aktual.budget.model.FirstDayOfWeek
import aktual.budget.model.NumberFormat
import aktual.prefs.vm.BooleanPreference
import aktual.prefs.vm.ListPreference
import androidx.compose.runtime.Immutable

@Immutable
data class FormatSettingsState(
  val numberFormat: ListPreference<NumberFormat>,
  val dateFormat: ListPreference<DateFormat>,
  val firstDayOfWeek: ListPreference<FirstDayOfWeek>,
  val hideFraction: BooleanPreference,
)
