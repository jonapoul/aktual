package aktual.settings.ui.theme

import aktual.core.model.ThemeId
import androidx.compose.runtime.Immutable

@Immutable
interface ThemeSettingsNavigator {
  fun back(): Boolean

  fun inspectTheme(id: ThemeId)
}
