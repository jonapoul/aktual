package aktual.core.prefs

import aktual.core.model.DarkColorSchemeType
import aktual.core.model.RegularColorSchemeType
import aktual.core.model.ServerUrl
import aktual.core.model.Token
import dev.jonpoulton.preferences.core.Preference

/** Prefs which are kept on this device, but apply across all budgets */
interface AppGlobalPreferences {
  val token: Preference<Token?>
  val regularColorScheme: Preference<RegularColorSchemeType>
  val darkColorScheme: Preference<DarkColorSchemeType>
  val serverUrl: Preference<ServerUrl?>
  val showBottomBar: Preference<Boolean>
}
