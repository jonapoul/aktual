package aktual.settings.ui.theme

import aktual.core.theme.Theme
import androidx.compose.runtime.Immutable

@Immutable
interface ThemeSettingsNavigator {
  fun back(): Boolean

  fun inspectTheme(id: Theme.Id)
}
