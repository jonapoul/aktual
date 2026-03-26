package aktual.prefs

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState

interface WindowStatePreferences {
  val position: Preference<WindowPosition>

  val size: Preference<DpSize>

  val isMinimized: Preference<Boolean>

  val placement: Preference<WindowPlacement>

  suspend fun save(state: WindowState)
}
