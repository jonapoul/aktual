package aktual.core.prefs

import dev.jonpoulton.preferences.core.Preferences

interface JvmPreferences : Preferences {
  fun flush()
}
