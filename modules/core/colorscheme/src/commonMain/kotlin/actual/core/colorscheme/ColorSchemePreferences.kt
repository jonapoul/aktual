package actual.core.colorscheme

import dev.jonpoulton.preferences.core.IntSerializer
import dev.jonpoulton.preferences.core.Preference
import dev.jonpoulton.preferences.core.Preferences
import dev.jonpoulton.preferences.core.enumOrdinalSerializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

class ColorSchemePreferences @Inject constructor(private val prefs: Preferences) {
  val colorSchemeType: Preference<ColorSchemeType>
    get() = prefs
      .getObject(key = "colorSchemeType", Serializer, default = ColorSchemeType.System)

  fun stateFlow(scope: CoroutineScope): StateFlow<ColorSchemeType> = colorSchemeType
    .asFlow()
    .stateIn(scope, SharingStarted.Eagerly, colorSchemeType.default)

  private object Serializer : IntSerializer<ColorSchemeType> by enumOrdinalSerializer<ColorSchemeType>()
}
