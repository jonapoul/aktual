package aktual.prefs.ui.root

import androidx.compose.runtime.Immutable

internal sealed interface SettingsAction

internal data object NavBack : SettingsAction

internal data object NavToThemeSettings : SettingsAction

internal data object NavToScheduleSettings : SettingsAction

@Immutable
internal fun interface SettingsActionHandler {
  operator fun invoke(action: SettingsAction)
}
