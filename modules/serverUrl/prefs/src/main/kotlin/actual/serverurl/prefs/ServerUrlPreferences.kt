package actual.serverurl.prefs

import actual.core.model.ServerUrl
import dev.jonpoulton.preferences.core.Preference
import dev.jonpoulton.preferences.core.Preferences
import dev.jonpoulton.preferences.core.SimpleNullableStringSerializer
import javax.inject.Inject

class ServerUrlPreferences @Inject constructor(
  prefs: Preferences,
) {
  val url: Preference<ServerUrl?> = prefs
    .getNullableObject(key = "serverUrl", default = null, serializer = ServerUrlSerializer)

  private companion object {
    private val ServerUrlSerializer = SimpleNullableStringSerializer { url -> url?.let(::ServerUrl) }
  }
}
