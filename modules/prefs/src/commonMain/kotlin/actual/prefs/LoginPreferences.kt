package actual.prefs

import actual.account.model.LoginToken
import dev.jonpoulton.preferences.core.Preference
import dev.jonpoulton.preferences.core.Preferences
import dev.jonpoulton.preferences.core.SimpleNullableStringSerializer
import javax.inject.Inject

class LoginPreferences @Inject constructor(prefs: Preferences) {
  val token: Preference<LoginToken?> = prefs.getNullableObject("token", LoginTokenSerializer, default = null)

  private companion object {
    val LoginTokenSerializer = SimpleNullableStringSerializer { token -> token?.let(::LoginToken) }
  }
}
