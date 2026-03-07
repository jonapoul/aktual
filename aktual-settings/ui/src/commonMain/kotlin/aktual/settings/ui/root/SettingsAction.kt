package aktual.settings.ui.root

import androidx.compose.runtime.Immutable

@Immutable
internal sealed interface SettingsAction {
  data object NavBack : SettingsAction

  data object NavToThemeSettings : SettingsAction
}
