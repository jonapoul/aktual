package actual.prefs

import dev.jonpoulton.preferences.core.Preference
import dev.jonpoulton.preferences.core.Preferences
import javax.inject.Inject

class BottomBarPreferences @Inject constructor(prefs: Preferences) {
  val show: Preference<Boolean> = prefs.getBoolean("bottomBar.show", default = true)
}
