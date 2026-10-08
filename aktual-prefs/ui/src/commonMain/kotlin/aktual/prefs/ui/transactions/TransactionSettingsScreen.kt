package aktual.prefs.ui.transactions

import aktual.core.icons.material.FormatListBulleted
import aktual.core.icons.material.MaterialIcons
import aktual.core.l10n.Strings
import aktual.core.nav.BackNavigator
import aktual.core.theme.hasAlternateRowColour
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.BottomSpacing
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.Dimens
import aktual.core.ui.NavBackIconButton
import aktual.core.ui.PageBackground
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.hazedTopBar
import aktual.core.ui.hazedTopBarContent
import aktual.core.ui.hazedTopBarContentPadding
import aktual.core.ui.rememberHazedTopBarState
import aktual.core.ui.scrollbar
import aktual.core.ui.transparentTopAppBarColors
import aktual.prefs.ui.BooleanPreferenceItem
import aktual.prefs.vm.BooleanPreference
import aktual.prefs.vm.transactions.TransactionSettingsState
import aktual.prefs.vm.transactions.TransactionSettingsViewModel
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel

@Composable
fun TransactionSettingsScreen(
  back: BackNavigator,
  viewModel: TransactionSettingsViewModel = metroViewModel<TransactionSettingsViewModel>(),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()

  TransactionSettingsScaffold(
    state = state,
    onAction = { action ->
      when (action) {
        NavBack -> back()
      }
    },
  )
}

@Composable
private fun TransactionSettingsScaffold(
  state: TransactionSettingsState,
  onAction: TransactionSettingsActionHandler,
) {
  val listState = rememberLazyListState()
  val hazeState = rememberHazedTopBarState()

  Scaffold(
    topBar = {
      TopAppBar(
        modifier = Modifier.hazedTopBar(hazeState, listState),
        colors = colors.transparentTopAppBarColors(),
        navigationIcon = { NavBackIconButton { onAction(NavBack) } },
        title = { Text(Strings.settingsTransactionsToolbar) },
      )
    },
  ) { innerPadding ->
    Box {
      PageBackground()
      TransactionSettingsContent(
        modifier = Modifier.hazedTopBarContent(hazeState, innerPadding),
        contentPadding = hazedTopBarContentPadding(hazeState, innerPadding),
        listState = listState,
        state = state,
      )
    }
  }
}

@Composable
private fun TransactionSettingsContent(
  state: TransactionSettingsState,
  contentPadding: PaddingValues,
  listState: LazyListState,
  modifier: Modifier = Modifier,
) {
  LazyColumn(
    modifier = modifier.fillMaxSize().scrollbar(listState).padding(Dimens.Large),
    state = listState,
    contentPadding = contentPadding,
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    item {
      BooleanPreferenceItem(
        preference = state.alternateRowColours,
        title = Strings.settingsTransactionsAlternateRows,
        subtitle =
          if (colors.hasAlternateRowColour) {
            Strings.settingsTransactionsAlternateRowsSupported
          } else {
            Strings.settingsTransactionsAlternateRowsUnsupported
          },
        icon = MaterialIcons.FormatListBulleted,
      )
    }
    item { BottomSpacing() }
  }
}

@PortraitPreview
@Composable
private fun PreviewTransactionSettingsScaffold(
  @PreviewParameter(TransactionSettingsProvider::class)
  params: ColoredParams<TransactionSettingsState>,
) = PreviewWithColoredParams(params) { TransactionSettingsScaffold(state = this, onAction = {}) }

private class TransactionSettingsProvider :
  ColoredParameterProvider<TransactionSettingsState>(
    TransactionSettingsState(alternateRowColours = BooleanPreference(value = true)),
    TransactionSettingsState(alternateRowColours = BooleanPreference(value = false)),
  )
