package aktual.core.prefs

import aktual.core.model.ThemeId

interface ThemePreferences {
  // Switch between light and dark/midnight, depending on system time (and nightTheme)
  val useSystemDefault: Preference<Boolean>

  // Only relevant if useSystemDefault is true
  val nightTheme: Preference<ThemeId>

  // Only relevant if useSystemDefault is false
  val constantTheme: NullablePreference<ThemeId>
}
