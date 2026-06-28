package aktual.prefs

import aktual.budget.model.BudgetId
import aktual.core.model.ServerUrl
import aktual.core.model.Token

/** Prefs which are kept on this device, but apply across all budgets */
interface AppPreferences {
  val token: NullablePreference<Token>
  val serverUrl: NullablePreference<ServerUrl>
  val isPrivacyEnabled: Preference<Boolean>
  val mostRecentNumBudgets: Preference<Int>
  val lastOpenedBudgetId: NullablePreference<BudgetId>

  // Ordered BudgetTab names for the nav grid; empty means "not customised"
  val navGridOrder: Preference<List<String>>

  // The "#RRGGBB" colour last applied to a tag, used to seed the colour for the next new tag.
  // Null until the user has saved a coloured tag
  val lastUsedTagColor: NullablePreference<String>
}
