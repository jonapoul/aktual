package actual.login.prefs

import actual.core.model.LoginToken
import dev.jonpoulton.preferences.core.Preference
import dev.jonpoulton.preferences.core.Preferences
import dev.jonpoulton.preferences.core.SimpleNullableStringSerializer
import javax.inject.Inject

class LoginPreferences @Inject constructor(
  prefs: Preferences,
) {
  val token: Preference<LoginToken?> = prefs
    .getNullableObject(key = "token", default = null, serializer = LoginTokenSerializer)

  private companion object {
    val LoginTokenSerializer = SimpleNullableStringSerializer { token -> token?.let(::LoginToken) }
  }
}
