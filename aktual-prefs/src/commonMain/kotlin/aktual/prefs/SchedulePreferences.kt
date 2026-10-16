package aktual.prefs

import aktual.budget.model.ScheduleSort
import aktual.budget.model.SortDirection

interface SchedulePreferences {
  val showCompleted: Preference<Boolean>
  val sortField: Preference<ScheduleSort.Field>
  val sortDirection: Preference<SortDirection>
}
