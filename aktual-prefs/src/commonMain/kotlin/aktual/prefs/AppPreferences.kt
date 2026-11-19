package aktual.prefs

import aktual.budget.model.BudgetId
import aktual.core.model.CrashFile
import aktual.core.model.ServerUrl
import aktual.core.model.Token

/** Prefs which are kept on this device, but apply across all budgets */
interface AppPreferences {
  val token: NullablePreference<Token>
  val serverUrl: NullablePreference<ServerUrl>
  val mostRecentNumBudgets: Preference<Int>
  val lastOpenedBudgetId: NullablePreference<BudgetId>
  val lastSeenCrash: NullablePreference<CrashFile>
  val checkForNightlyUpdates: Preference<Boolean>
}
