package aktual.core.theme

import aktual.core.prefs.NullablePreference
import aktual.core.prefs.Preference

interface ThemePreferences {
  // Switch between light and dark/midnight, depending on system time (and nightTheme)
  val useSystemDefault: Preference<Boolean>

  // Only relevant if useSystemDefault is true
  val nightTheme: Preference<Theme.Id>

  // Only relevant if useSystemDefault is false
  val constantTheme: NullablePreference<Theme.Id>
}
