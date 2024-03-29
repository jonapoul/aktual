package dev.jonpoulton.actual.login.prefs

import com.fredporciuncula.flow.preferences.FlowSharedPreferences
import com.fredporciuncula.flow.preferences.Preference
import dev.jonpoulton.actual.core.model.LoginToken
import dev.jonpoulton.actual.core.prefs.simpleNullableMap
import javax.inject.Inject

class LoginPreferences @Inject constructor(prefs: FlowSharedPreferences) {
  val token: Preference<LoginToken?> = prefs
    .getNullableString(key = "token", defaultValue = null)
    .simpleNullableMap(::LoginToken)
}
