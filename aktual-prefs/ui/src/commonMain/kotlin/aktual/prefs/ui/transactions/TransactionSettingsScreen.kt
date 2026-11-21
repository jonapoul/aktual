package aktual.prefs.ui.transactions

import aktual.core.icons.AktualIcons
import aktual.core.icons.Split
import aktual.core.icons.material.FormatListBulleted
import aktual.core.icons.material.MaterialIcons
import aktual.core.l10n.Strings
import aktual.core.nav.BackNavigator
import aktual.core.nav.SettingsHighlight
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.ColorParameters
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColors
import aktual.prefs.ui.BooleanPreferenceItem
import aktual.prefs.ui.PreviewSettingsScaffold
import aktual.prefs.vm.BooleanPreference
import aktual.prefs.vm.transactions.TransactionSettingsState
import aktual.prefs.vm.transactions.TransactionSettingsViewModel
import aktual.theme.Colors
import aktual.theme.hasAlternateRowColour
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel

@Composable
fun TransactionSettingsScreen(
  back: BackNavigator,
  highlight: SettingsHighlight?,
  viewModel: TransactionSettingsViewModel = metroViewModel<TransactionSettingsViewModel>(),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  TransactionSettingsScaffold(state = state, highlight = highlight, onBack = back::invoke)
}

@Composable
private fun TransactionSettingsScaffold(
  state: TransactionSettingsState,
  onBack: () -> Unit,
  highlight: SettingsHighlight? = null,
) {
  PreviewSettingsScaffold(
    title = Strings.settingsTransactions,
    description = Strings.settingsTransactionsDesc,
    onBack = onBack,
    preview = {
      TransactionsPreview(
        alternateRowColours = state.alternateRowColours.value,
        expandSplits = state.expandSplits.value,
      )
    },
    highlight = highlight,
  ) {
    item {
      // Only themes with their own alternate row colour shade rows, see hasAlternateRowColour
      val supported = colors.hasAlternateRowColour
      BooleanPreferenceItem(
        preference = state.alternateRowColours.copy(enabled = supported),
        title = Strings.transactionsViewAlternateRows,
        subtitle = if (supported) null else Strings.transactionsViewAlternateRowsUnsupported,
        icon = MaterialIcons.FormatListBulleted,
      )
    }
    item {
      BooleanPreferenceItem(
        preference = state.expandSplits,
        title = Strings.transactionsViewExpandSplits,
        subtitle = null,
        icon = AktualIcons.Split,
      )
    }
  }
}

@PortraitPreview
@Composable
private fun PreviewTransactionSettingsScaffold(
  @PreviewParameter(ColorParameters::class) colors: Colors,
) =
  PreviewWithColors(colors) {
    TransactionSettingsScaffold(
      onBack = {},
      state =
        TransactionSettingsState(
          alternateRowColours = BooleanPreference(true),
          expandSplits = BooleanPreference(true),
        ),
    )
  }
