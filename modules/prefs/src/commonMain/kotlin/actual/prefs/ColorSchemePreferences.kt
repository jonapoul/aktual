package actual.prefs

import actual.core.model.ColorSchemeType
import dev.jonpoulton.preferences.core.IntSerializer
import dev.jonpoulton.preferences.core.Preference
import dev.jonpoulton.preferences.core.Preferences
import dev.jonpoulton.preferences.core.enumOrdinalSerializer
import javax.inject.Inject

class ColorSchemePreferences @Inject constructor(prefs: Preferences) {
  val type: Preference<ColorSchemeType> = prefs.getObject(key = "colorSchemeType", Serializer, ColorSchemeType.System)

  private object Serializer : IntSerializer<ColorSchemeType> by enumOrdinalSerializer<ColorSchemeType>()
}
