package aktual.prefs.ui.root

import androidx.compose.runtime.Immutable

@Immutable internal sealed interface SettingsAction

internal data object NavBack : SettingsAction

internal data object NavToThemeSettings : SettingsAction

@Immutable
internal fun interface SettingsActionHandler {
  operator fun invoke(action: SettingsAction)
}
