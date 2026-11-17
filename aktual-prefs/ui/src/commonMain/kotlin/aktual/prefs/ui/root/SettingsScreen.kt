package aktual.prefs.ui.root

import aktual.budget.model.Amount
import aktual.core.icons.AktualIcons
import aktual.core.icons.SearchAlternate
import aktual.core.icons.System
import aktual.core.icons.Tuning
import aktual.core.icons.material.Apps
import aktual.core.icons.material.CalendarViewWeek
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Numbers
import aktual.core.icons.material.ReceiptLong
import aktual.core.icons.material.Security
import aktual.core.icons.material.Speed125
import aktual.core.l10n.Strings
import aktual.core.nav.AmountSettingsNavigator
import aktual.core.nav.AppIconSettingsNavigator
import aktual.core.nav.BackNavigator
import aktual.core.nav.DateSettingsNavigator
import aktual.core.nav.FormatSettingsNavigator
import aktual.core.nav.SearchSettingsNavigator
import aktual.core.nav.SecuritySettingsNavigator
import aktual.core.nav.SystemUiSettingsNavigator
import aktual.core.nav.ThemeSettingsNavigator
import aktual.core.nav.TransactionSettingsNavigator
import aktual.core.theme.Colors
import aktual.core.ui.AktualIconButton
import aktual.core.ui.ColorParameters
import aktual.core.ui.OnAction
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.formatted
import aktual.core.ui.formattedString
import aktual.prefs.components.PreferenceGroup
import aktual.prefs.ui.SettingsListScaffold
import aktual.prefs.vm.root.SettingsViewModel
import aktual.prefs.vm.security.ShouldShowSecuritySettings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.datetime.LocalDate

@Composable
fun SettingsScreen(
  back: BackNavigator,
  toSearch: SearchSettingsNavigator,
  toThemeSettings: ThemeSettingsNavigator,
  toSystemUiSettings: SystemUiSettingsNavigator,
  toSecuritySettings: SecuritySettingsNavigator,
  toFormatSettings: FormatSettingsNavigator,
  toTransactionSettings: TransactionSettingsNavigator,
  toAppIconSettings: AppIconSettingsNavigator,
  toAmountSettings: AmountSettingsNavigator,
  toDateSettings: DateSettingsNavigator,
  viewModel: SettingsViewModel = metroViewModel<SettingsViewModel>(),
) {
  val budgetName by viewModel.budgetName.collectAsStateWithLifecycle()
  SettingsScaffold(
    budgetName = budgetName,
    onAction = { action ->
      when (action) {
        NavBack -> back()
        OpenSearch -> toSearch()
        NavToThemeSettings -> toThemeSettings()
        NavToSystemUiSettings -> toSystemUiSettings()
        NavToSecuritySettings -> toSecuritySettings()
        NavToFormatSettings -> toFormatSettings()
        NavToTransactionSettings -> toTransactionSettings()
        NavToAppIconSettings -> toAppIconSettings()
        NavToAmountSettings -> toAmountSettings()
        NavToDateSettings -> toDateSettings()
      }
    },
  )
}

// Settings synced with the open budget come first, then the ones only stored on this device
@Composable
private fun SettingsScaffold(budgetName: String?, onAction: OnAction<SettingsAction>) {
  SettingsListScaffold(
    title = Strings.settingsToolbar,
    description = null,
    onBack = { onAction(NavBack) },
    actions = {
      AktualIconButton(
        imageVector = AktualIcons.SearchAlternate,
        contentDescription = Strings.settingsSearch,
        onClick = { onAction(OpenSearch) },
      )
    },
  ) {
    if (budgetName != null) {
      item {
        PreferenceGroup(
          title = budgetName.ifBlank { Strings.settingsBudgetUnnamed },
          subtitle = Strings.settingsBudgetSynced,
        ) {
          SubSettingsItem(
            title = Strings.settingsAmounts,
            subtitle = AmountsExample.formattedString(isPrivacyEnabled = false),
            icon = MaterialIcons.Speed125,
            onClick = { onAction(NavToAmountSettings) },
          )
          SubSettingsItem(
            title = Strings.settingsFormatDatesGroup,
            subtitle = DatesExample.formatted(),
            icon = MaterialIcons.CalendarViewWeek,
            onClick = { onAction(NavToDateSettings) },
          )
        }
      }
    }

    item {
      PreferenceGroup(
        title = Strings.settingsDeviceGroup,
        subtitle = Strings.settingsDeviceDesc,
      ) {
        SubSettingsItem(
          title = Strings.settingsTheme,
          icon = AktualIcons.System,
          onClick = { onAction(NavToThemeSettings) },
        )
        SubSettingsItem(
          title = Strings.settingsUiGroup,
          icon = AktualIcons.Tuning,
          onClick = { onAction(NavToSystemUiSettings) },
        )
        SubSettingsItem(
          title = Strings.settingsAppIcon,
          icon = MaterialIcons.Apps,
          onClick = { onAction(NavToAppIconSettings) },
        )
        if (ShouldShowSecuritySettings) {
          SubSettingsItem(
            title = Strings.settingsSecurity,
            icon = MaterialIcons.Security,
            onClick = { onAction(NavToSecuritySettings) },
          )
        }
        SubSettingsItem(
          title = Strings.settingsFormatGroup,
          icon = MaterialIcons.Numbers,
          onClick = { onAction(NavToFormatSettings) },
        )
        SubSettingsItem(
          title = Strings.settingsTransactions,
          icon = MaterialIcons.ReceiptLong,
          onClick = { onAction(NavToTransactionSettings) },
        )
      }
    }
  }
}

private val AmountsExample = Amount(1_234.56)

// Day and month both fit in either position, so the order shows
private val DatesExample = LocalDate(year = 2026, month = 2, day = 3)

@PortraitPreview
@Composable
private fun PreviewSettingsScaffold(@PreviewParameter(ColorParameters::class) colors: Colors) =
  PreviewWithColors(colors) { SettingsScaffold(budgetName = "My Budget", onAction = {}) }

@PortraitPreview
@Composable
private fun PreviewSettingsScaffoldNoBudget(
  @PreviewParameter(ColorParameters::class) colors: Colors,
) = PreviewWithColors(colors) { SettingsScaffold(budgetName = null, onAction = {}) }
