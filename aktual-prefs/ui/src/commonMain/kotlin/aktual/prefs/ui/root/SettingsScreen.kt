package aktual.prefs.ui.root

import aktual.core.icons.AktualIcons
import aktual.core.icons.SearchAlternate
import aktual.core.icons.System
import aktual.core.icons.Tuning
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Numbers
import aktual.core.icons.material.Security
import aktual.core.l10n.Strings
import aktual.core.nav.BackNavigator
import aktual.core.nav.FormatSettingsNavigator
import aktual.core.nav.SearchSettingsNavigator
import aktual.core.nav.SecuritySettingsNavigator
import aktual.core.nav.SystemUiSettingsNavigator
import aktual.core.nav.ThemeSettingsNavigator
import aktual.core.theme.Colors
import aktual.core.ui.BareIconButton
import aktual.core.ui.ColorParameters
import aktual.core.ui.OnAction
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColors
import aktual.prefs.ui.SettingsListScaffold
import aktual.prefs.vm.security.ShouldShowSecuritySettings
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewParameter

@Composable
fun SettingsScreen(
  back: BackNavigator,
  toSearch: SearchSettingsNavigator,
  toThemeSettings: ThemeSettingsNavigator,
  toSystemUiSettings: SystemUiSettingsNavigator,
  toSecuritySettings: SecuritySettingsNavigator,
  toFormatSettings: FormatSettingsNavigator,
) {
  SettingsScaffold(
    onAction = { action ->
      when (action) {
        NavBack -> back()
        OpenSearch -> toSearch()
        NavToThemeSettings -> toThemeSettings()
        NavToSystemUiSettings -> toSystemUiSettings()
        NavToSecuritySettings -> toSecuritySettings()
        NavToFormatSettings -> toFormatSettings()
      }
    },
  )
}

@Composable
private fun SettingsScaffold(onAction: OnAction<SettingsAction>) {
  SettingsListScaffold(
    title = Strings.settingsToolbar,
    description = null,
    onBack = { onAction(NavBack) },
    actions = {
      BareIconButton(
        imageVector = AktualIcons.SearchAlternate,
        contentDescription = Strings.settingsSearch,
        onClick = { onAction(OpenSearch) },
      )
    },
  ) {
    item {
      SubSettingsItem(
        title = Strings.settingsTheme,
        icon = AktualIcons.System,
        onClick = { onAction(NavToThemeSettings) },
      )
    }
    item {
      SubSettingsItem(
        title = Strings.settingsUiGroup,
        icon = AktualIcons.Tuning,
        onClick = { onAction(NavToSystemUiSettings) },
      )
    }
    if (ShouldShowSecuritySettings) {
      item {
        SubSettingsItem(
          title = Strings.settingsSecurity,
          icon = MaterialIcons.Security,
          onClick = { onAction(NavToSecuritySettings) },
        )
      }
    }
    item {
      SubSettingsItem(
        title = Strings.settingsFormatGroup,
        icon = MaterialIcons.Numbers,
        onClick = { onAction(NavToFormatSettings) },
      )
    }
  }
}

@PortraitPreview
@Composable
private fun PreviewSettingsScaffold(@PreviewParameter(ColorParameters::class) colors: Colors) =
  PreviewWithColors(colors) { SettingsScaffold(onAction = {}) }
