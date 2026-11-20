package aktual.prefs

import aktual.core.model.ThemeId

interface ThemePreferences {
  // Switch between dayTheme and nightTheme, following the system's light or dark mode
  val useSystemDefault: Preference<Boolean>

  // Only relevant if useSystemDefault is true
  val dayTheme: Preference<ThemeId>

  // Only relevant if useSystemDefault is true
  val nightTheme: Preference<ThemeId>

  // Only relevant if useSystemDefault is false
  val constantTheme: NullablePreference<ThemeId>
}
