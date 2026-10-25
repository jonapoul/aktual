package aktual.prefs.ui.root

import aktual.core.icons.AktualIcons
import aktual.core.icons.Calendar3
import aktual.core.icons.System
import aktual.core.icons.Tuning
import aktual.core.icons.material.CurrencyPound
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Numbers
import aktual.core.icons.material.ReceiptLong
import aktual.core.icons.material.Security
import aktual.core.l10n.Strings
import aktual.core.nav.BackNavigator
import aktual.core.nav.CurrencySettingsNavigator
import aktual.core.nav.FormatSettingsNavigator
import aktual.core.nav.ScheduleSettingsNavigator
import aktual.core.nav.SecuritySettingsNavigator
import aktual.core.nav.SystemUiSettingsNavigator
import aktual.core.nav.ThemeSettingsNavigator
import aktual.core.nav.TransactionSettingsNavigator
import aktual.core.theme.Colors
import aktual.core.ui.ColoredParameters
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColors
import aktual.prefs.ui.SettingsListScaffold
import aktual.prefs.vm.security.ShouldShowSecuritySettings
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewParameter

@Composable
fun SettingsScreen(
  back: BackNavigator,
  toThemeSettings: ThemeSettingsNavigator,
  toScheduleSettings: ScheduleSettingsNavigator,
  toTransactionSettings: TransactionSettingsNavigator,
  toSystemUiSettings: SystemUiSettingsNavigator,
  toSecuritySettings: SecuritySettingsNavigator,
  toFormatSettings: FormatSettingsNavigator,
  toCurrencySettings: CurrencySettingsNavigator,
) {
  SettingsScaffold(
    onAction = { action ->
      when (action) {
        NavBack -> back()
        NavToThemeSettings -> toThemeSettings()
        NavToScheduleSettings -> toScheduleSettings()
        NavToTransactionSettings -> toTransactionSettings()
        NavToSystemUiSettings -> toSystemUiSettings()
        NavToSecuritySettings -> toSecuritySettings()
        NavToFormatSettings -> toFormatSettings()
        NavToCurrencySettings -> toCurrencySettings()
      }
    },
  )
}

@Composable
private fun SettingsScaffold(onAction: SettingsActionHandler) {
  SettingsListScaffold(
    title = Strings.settingsToolbar,
    description = null,
    onBack = { onAction(NavBack) },
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
        title = Strings.settingsSchedules,
        icon = AktualIcons.Calendar3,
        onClick = { onAction(NavToScheduleSettings) },
      )
    }
    item {
      SubSettingsItem(
        title = Strings.settingsTransactions,
        icon = MaterialIcons.ReceiptLong,
        onClick = { onAction(NavToTransactionSettings) },
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
    item {
      SubSettingsItem(
        title = Strings.settingsCurrency,
        icon = MaterialIcons.CurrencyPound,
        onClick = { onAction(NavToCurrencySettings) },
      )
    }
  }
}

@PortraitPreview
@Composable
private fun PreviewSettingsScaffold(@PreviewParameter(ColoredParameters::class) colors: Colors) =
  PreviewWithColors(colors) { SettingsScaffold(onAction = {}) }
