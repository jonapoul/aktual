package aktual.prefs

import aktual.budget.model.PayeeLocationSort
import aktual.budget.model.SortDirection

interface PayeeLocationPreferences {
  val sortField: Preference<PayeeLocationSort.Field>
  val sortDirection: Preference<SortDirection>
}
