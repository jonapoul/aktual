package actual.prefs

import dev.jonpoulton.preferences.core.Preference
import dev.jonpoulton.preferences.core.Preferences
import javax.inject.Inject

class StatusBarPreferences @Inject constructor(prefs: Preferences) {
  val showStatusBar: Preference<Boolean> = prefs.getBoolean("statusBar.show", default = true)
}
