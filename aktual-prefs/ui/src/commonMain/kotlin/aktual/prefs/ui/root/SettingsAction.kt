package aktual.prefs.ui.root

internal sealed interface SettingsAction

internal data object NavBack : SettingsAction

internal data object OpenSearch : SettingsAction

internal data object NavToThemeSettings : SettingsAction

internal data object NavToSystemUiSettings : SettingsAction

internal data object NavToSecuritySettings : SettingsAction

internal data object NavToFormatSettings : SettingsAction
