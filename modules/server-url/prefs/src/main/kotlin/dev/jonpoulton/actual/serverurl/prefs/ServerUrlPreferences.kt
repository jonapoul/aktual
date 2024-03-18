package dev.jonpoulton.actual.serverurl.prefs

import com.fredporciuncula.flow.preferences.FlowSharedPreferences
import com.fredporciuncula.flow.preferences.Preference
import com.fredporciuncula.flow.preferences.map
import dev.jonpoulton.actual.core.model.ServerUrl
import javax.inject.Inject

class ServerUrlPreferences @Inject constructor(prefs: FlowSharedPreferences) {
  val url: Preference<ServerUrl?> = prefs
    .getNullableString(key = "serverUrl", defaultValue = null)
    .map(mapper = { it?.let(::ServerUrl) }, reverse = { it?.toString() })
}
