package actual.prefs

import actual.account.model.LoginToken
import actual.core.model.ColorSchemeType
import actual.core.model.ServerUrl
import dev.jonpoulton.preferences.core.IntSerializer
import dev.jonpoulton.preferences.core.Preference
import dev.jonpoulton.preferences.core.Preferences
import dev.jonpoulton.preferences.core.SimpleNullableStringSerializer
import dev.jonpoulton.preferences.core.enumOrdinalSerializer
import javax.inject.Inject

class AppLocalPreferences @Inject constructor(preferences: Preferences) {
  val loginToken: Preference<LoginToken?> = preferences
    .getNullableObject(key = "token", LoginTokenSerializer, default = null)

  val colorSchemeType: Preference<ColorSchemeType> = preferences
    .getObject(key = "colorSchemeType", ColorSchemeSerializer, default = ColorSchemeType.System)

  val serverUrl: Preference<ServerUrl?> = preferences
    .getNullableObject(key = "serverUrl", ServerUrlSerializer, default = null)

  val showBottomBar: Preference<Boolean> = preferences.getBoolean(key = "bottomBar.show", default = true)

  private object ColorSchemeSerializer : IntSerializer<ColorSchemeType> by enumOrdinalSerializer<ColorSchemeType>()

  private companion object {
    val LoginTokenSerializer = SimpleNullableStringSerializer { token -> token?.let(::LoginToken) }
    val ServerUrlSerializer = SimpleNullableStringSerializer { url -> url?.let(::ServerUrl) }
  }
}
