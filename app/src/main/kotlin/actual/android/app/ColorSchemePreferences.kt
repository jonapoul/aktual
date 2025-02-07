package actual.android.app

import actual.core.ui.ColorSchemeType
import dev.jonpoulton.preferences.android.map
import dev.jonpoulton.preferences.core.Preference
import dev.jonpoulton.preferences.core.Preferences
import javax.inject.Inject

internal class ColorSchemePreferences @Inject constructor(private val prefs: Preferences) {
  val colorSchemeType: Preference<ColorSchemeType>
    get() = prefs
      .getInt(key = "colorSchemeType", default = -1)
      .map(
        mapper = { int -> if (int == -1) ColorSchemeType.System else ColorSchemeType.entries[int] },
        reverse = { type -> type.ordinal },
      )
}
