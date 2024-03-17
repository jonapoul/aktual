package dev.jonpoulton.actual.serverurl.prefs

import com.fredporciuncula.flow.preferences.FlowSharedPreferences
import com.fredporciuncula.flow.preferences.Preference
import com.fredporciuncula.flow.preferences.map
import dev.jonpoulton.actual.serverurl.model.Protocol
import javax.inject.Inject

class ServerUrlPreferences @Inject constructor(prefs: FlowSharedPreferences) {
  val url: Preference<String?> = prefs.getNullableString(key = "serverUrl", defaultValue = null)

  val protocol: Preference<Protocol?> = prefs.getNullableString(key = "serverProtocol", defaultValue = null)
    .map(
      mapper = { if (it != null) Protocol.fromString(it) else null },
      reverse = { it?.toString() },
    )
}
