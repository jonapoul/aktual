package dev.jonpoulton.actual.serverurl.vm

import com.fredporciuncula.flow.preferences.FlowSharedPreferences
import com.fredporciuncula.flow.preferences.Preference
import com.fredporciuncula.flow.preferences.map
import javax.inject.Inject

internal class ServerUrlPreferences @Inject constructor(prefs: FlowSharedPreferences) {
  val serverUrl: Preference<String?> = prefs.getNullableString(key = "serverUrl", defaultValue = null)

  val protocol: Preference<Protocol?> = prefs.getNullableString(key = "serverProtocol", defaultValue = null)
    .map(
      mapper = { if (it != null) Protocol.fromString(it) else null },
      reverse = { it?.toString() },
    )
}
