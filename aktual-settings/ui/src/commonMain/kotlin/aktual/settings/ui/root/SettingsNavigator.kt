package aktual.settings.ui.root

import androidx.compose.runtime.Immutable

@Immutable
interface SettingsNavigator {
  fun back(): Boolean

  fun toThemeSettings()
}
