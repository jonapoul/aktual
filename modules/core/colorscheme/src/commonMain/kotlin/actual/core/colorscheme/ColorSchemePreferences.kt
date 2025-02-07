package actual.core.colorscheme

import dev.jonpoulton.preferences.core.IntSerializer
import dev.jonpoulton.preferences.core.Preference
import dev.jonpoulton.preferences.core.Preferences
import dev.jonpoulton.preferences.core.enumOrdinalSerializer
import javax.inject.Inject

class ColorSchemePreferences @Inject constructor(private val prefs: Preferences) {
  val colorSchemeType: Preference<ColorSchemeType>
    get() = prefs
      .getObject(key = "colorSchemeType", Serializer, default = ColorSchemeType.System)

  private object Serializer : IntSerializer<ColorSchemeType> by enumOrdinalSerializer<ColorSchemeType>()
}
