package aktual.prefs.vm.format

import aktual.budget.model.DateFormat
import aktual.budget.model.FirstDayOfWeek
import aktual.core.model.DistanceUnit
import aktual.prefs.vm.ListPreference
import androidx.compose.runtime.Immutable

@Immutable
data class FormatSettingsState(
  val dateFormat: ListPreference<DateFormat>,
  val firstDayOfWeek: ListPreference<FirstDayOfWeek>,
  val distanceUnit: ListPreference<DistanceUnit>,
)
