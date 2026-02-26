package aktual.core.prefs

import aktual.core.model.ServerUrl
import aktual.core.model.Token
import dev.jonpoulton.preferences.core.Preference
import dev.jonpoulton.preferences.core.Preferences
import dev.jonpoulton.preferences.core.SimpleNullableStringSerializer
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

@Inject
@ContributesBinding(AppScope::class)
class AppGlobalPreferencesImpl(preferences: Preferences) : AppGlobalPreferences {
  override val token: Preference<Token?> =
    preferences.getNullableObject(key = "token", TokenSerializer, default = null)

  override val serverUrl: Preference<ServerUrl?> =
    preferences.getNullableObject(key = "serverUrl", ServerUrlSerializer, default = null)

  override val showBottomBar: Preference<Boolean> =
    preferences.getBoolean(key = "bottomBar.show", default = true)

  private companion object {
    val TokenSerializer = SimpleNullableStringSerializer { token -> token?.let(::Token) }
    val ServerUrlSerializer = SimpleNullableStringSerializer { url -> url?.let(::ServerUrl) }
  }
}
