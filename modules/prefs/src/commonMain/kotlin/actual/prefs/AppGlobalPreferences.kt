package actual.prefs

import actual.account.model.LoginToken
import actual.core.model.DarkColorSchemeType
import actual.core.model.RegularColorSchemeType
import actual.core.model.ServerUrl
import dev.jonpoulton.preferences.core.Preference
import dev.jonpoulton.preferences.core.Preferences
import dev.jonpoulton.preferences.core.SimpleNullableStringSerializer
import dev.jonpoulton.preferences.core.enumOrdinalSerializer
import javax.inject.Inject

/**
 * Prefs which are kept on this device, but apply across all budgets
 */
class AppGlobalPreferences @Inject constructor(preferences: Preferences) {
  val loginToken: Preference<LoginToken?> = preferences
    .getNullableObject(key = "token", LoginTokenSerializer, default = null)

  val regularColorScheme: Preference<RegularColorSchemeType> = preferences
    .getObject(key = "regularColorScheme", RegularColorSchemeSerializer, default = RegularColorSchemeType.System)

  val darkColorScheme: Preference<DarkColorSchemeType> = preferences
    .getObject(key = "darkColorScheme", DarkColorSchemeSerializer, default = DarkColorSchemeType.Dark)

  val serverUrl: Preference<ServerUrl?> = preferences
    .getNullableObject(key = "serverUrl", ServerUrlSerializer, default = null)

  val showBottomBar: Preference<Boolean> = preferences.getBoolean(key = "bottomBar.show", default = true)

  private companion object {
    val RegularColorSchemeSerializer = enumOrdinalSerializer<RegularColorSchemeType>()
    val DarkColorSchemeSerializer = enumOrdinalSerializer<DarkColorSchemeType>()
    val LoginTokenSerializer = SimpleNullableStringSerializer { token -> token?.let(::LoginToken) }
    val ServerUrlSerializer = SimpleNullableStringSerializer { url -> url?.let(::ServerUrl) }
  }
}
