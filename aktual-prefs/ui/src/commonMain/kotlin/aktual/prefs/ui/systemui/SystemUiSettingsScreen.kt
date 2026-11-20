package aktual.prefs.ui.systemui

import aktual.budget.model.BudgetTab
import aktual.core.icons.AktualIcons
import aktual.core.icons.ViewHide
import aktual.core.icons.ViewShow
import aktual.core.icons.material.BlurOn
import aktual.core.icons.material.Dialogs
import aktual.core.icons.material.Home
import aktual.core.icons.material.MaterialIcons
import aktual.core.l10n.Strings
import aktual.core.nav.BackNavigator
import aktual.core.nav.BarSettingsNavigator
import aktual.core.nav.DialogSettingsNavigator
import aktual.core.nav.SettingsHighlight
import aktual.core.theme.Colors
import aktual.core.ui.ColorParameters
import aktual.core.ui.LocalHazeConfig
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColors
import aktual.prefs.ui.BooleanPreferenceItem
import aktual.prefs.ui.ListPreferenceItem
import aktual.prefs.ui.SettingsListScaffold
import aktual.prefs.ui.bars.label
import aktual.prefs.ui.root.SubSettingsItem
import aktual.prefs.vm.BooleanPreference
import aktual.prefs.vm.ListPreference
import aktual.prefs.vm.systemui.SystemUiSettingsState
import aktual.prefs.vm.systemui.SystemUiSettingsViewModel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel

@Composable
fun SystemUiSettingsScreen(
  back: BackNavigator,
  toBarSettings: BarSettingsNavigator,
  toDialogSettings: DialogSettingsNavigator,
  highlight: SettingsHighlight?,
  viewModel: SystemUiSettingsViewModel = metroViewModel<SystemUiSettingsViewModel>(),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  SystemUiSettingsScaffold(
    state = state,
    highlight = highlight,
    onBack = back::invoke,
    onOpenBars = { toBarSettings() },
    onOpenDialogs = { toDialogSettings() },
  )
}

@Composable
private fun SystemUiSettingsScaffold(
  state: SystemUiSettingsState,
  onBack: () -> Unit,
  onOpenBars: () -> Unit,
  onOpenDialogs: () -> Unit,
  highlight: SettingsHighlight? = null,
) {
  SettingsListScaffold(
    title = Strings.settingsUiGroup,
    description = null,
    highlight = highlight,
    onBack = onBack,
  ) {
    item {
      ListPreferenceItem(
        preference = state.startTab,
        optionString = { it.label() },
        optionSuffix = null,
        title = Strings.settingsUiStartTab,
        subtitle = Strings.settingsUiStartTabDesc,
        icon = MaterialIcons.Home,
      )
    }
    item {
      BooleanPreferenceItem(
        preference = state.showStatusBar,
        title = Strings.settingsUiShowStatus,
        subtitle = null,
        icon = if (state.showStatusBar.value) AktualIcons.ViewShow else AktualIcons.ViewHide,
      )
    }
    item {
      SubSettingsItem(
        title = Strings.settingsUiBarsTitle,
        subtitle = LocalHazeConfig.current.appBarEffect.label(),
        icon = MaterialIcons.BlurOn,
        onClick = onOpenBars,
      )
    }
    item {
      SubSettingsItem(
        title = Strings.settingsUiDialogsTitle,
        subtitle =
          if (LocalHazeConfig.current.dialogs) {
            Strings.settingsDialogsBlurred
          } else {
            Strings.settingsDialogsNotBlurred
          },
        icon = MaterialIcons.Dialogs,
        onClick = onOpenDialogs,
      )
    }
  }
}

@Composable
private fun BudgetTab.label(): String =
  when (this) {
    BudgetTab.Home -> Strings.homeTitle
    Budget -> Strings.budgetingTitle
    Transactions -> Strings.transactionsTitle
    Reports -> Strings.reportsTitle
    Schedules -> Strings.listSchedulesTitle
    Rules -> Strings.rulesTitle
    Tags -> Strings.tagsTitle
    Payees -> Strings.payeesTitle
    BankSync -> Strings.bankSyncTitle
  }

@PortraitPreview
@Composable
private fun PreviewSystemUiSettingsScaffold(
  @PreviewParameter(ColorParameters::class) colors: Colors,
) =
  PreviewWithColors(colors) {
    SystemUiSettingsScaffold(
      onBack = {},
      onOpenBars = {},
      onOpenDialogs = {},
      state =
        SystemUiSettingsState(
          showStatusBar = BooleanPreference(true),
          startTab = ListPreference(BudgetTab.Home),
        ),
    )
  }
