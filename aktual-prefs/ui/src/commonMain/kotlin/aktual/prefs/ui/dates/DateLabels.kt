package aktual.prefs.ui.dates

import aktual.budget.model.DateFormat
import aktual.budget.model.FirstDayOfWeek
import aktual.core.l10n.Strings
import androidx.compose.runtime.Composable
import kotlinx.datetime.DayOfWeek

@Composable
internal fun DateFormat.label(): String =
  when (this) {
    MmDdYyyy -> Strings.settingsDateFormatMmDdYyyy
    DdMmYyyy -> Strings.settingsDateFormatDdMmYyyy
    YyyyMmDd -> Strings.settingsDateFormatYyyyMmDd
    MmDdYyyyDot -> Strings.settingsDateFormatMmDdYyyyDot
    DdMmYyyyDot -> Strings.settingsDateFormatDdMmYyyyDot
    DdMmYyyyDash -> Strings.settingsDateFormatDdMmYyyyDash
  }

@Composable
internal fun FirstDayOfWeek.label(): String =
  when (this) {
    Sunday -> Strings.weekSunday
    Monday -> Strings.weekMonday
    Tuesday -> Strings.weekTuesday
    Wednesday -> Strings.weekWednesday
    Thursday -> Strings.weekThursday
    Friday -> Strings.weekFriday
    Saturday -> Strings.weekSaturday
  }

// FirstDayOfWeek counts from Sunday = 0, DayOfWeek from Monday = 1
internal fun FirstDayOfWeek.toDayOfWeek(): DayOfWeek =
  if (this == Sunday) SUNDAY else DayOfWeek(value)
