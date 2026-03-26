package aktual.prefs

import aktual.budget.model.DateFormat
import aktual.budget.model.FirstDayOfWeek
import aktual.budget.model.NumberFormat

interface FormatPreferences {
  val hideFraction: Preference<Boolean>
  val dateFormat: Preference<DateFormat>
  val firstDayOfWeek: Preference<FirstDayOfWeek>
  val numberFormat: Preference<NumberFormat>
}
