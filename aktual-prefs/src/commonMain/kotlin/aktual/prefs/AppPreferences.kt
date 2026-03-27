package aktual.prefs

import aktual.core.model.ServerUrl
import aktual.core.model.Token

/** Prefs which are kept on this device, but apply across all budgets */
interface AppPreferences {
  val token: NullablePreference<Token>
  val serverUrl: NullablePreference<ServerUrl>
  val isPrivacyEnabled: Preference<Boolean>
  val mostRecentNumBudgets: Preference<Int>
}
