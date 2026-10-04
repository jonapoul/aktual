package aktual.budget.home.ui

import aktual.budget.home.vm.AccountsCardState
import aktual.budget.home.vm.HomeState
import aktual.budget.home.vm.HomeViewModel
import aktual.budget.model.AccountId
import aktual.core.l10n.Strings
import aktual.core.nav.BankSyncNavigator
import aktual.core.nav.TransactionsNavigator
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.BottomSpacing
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.Dimens
import aktual.core.ui.NavDrawerIconButton
import aktual.core.ui.PageBackground
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.transparentTopAppBarColors
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel

@Composable
internal fun HomeScreen(
  transactions: TransactionsNavigator,
  bankSync: BankSyncNavigator,
  modifier: Modifier = Modifier,
  viewModel: HomeViewModel = metroViewModel(),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  HomeScaffold(
    state = state,
    onClickAccount = { id -> transactions(id) },
    onClickSetUpAccounts = { bankSync() },
    modifier = modifier,
  )
}

@Composable
private fun HomeScaffold(
  state: HomeState,
  onClickAccount: (AccountId) -> Unit,
  onClickSetUpAccounts: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        colors = colors.transparentTopAppBarColors(),
        navigationIcon = { NavDrawerIconButton() },
        title = { HomeTitle(budgetName = state.budgetName) },
      )
    },
  ) { innerPadding ->
    Box(modifier = Modifier.fillMaxSize()) {
      PageBackground()

      Column(
        modifier =
          Modifier.fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(innerPadding)
            .padding(Dimens.Huge),
        verticalArrangement = Arrangement.spacedBy(Dimens.Huge),
      ) {
        AccountsCard(
          state = state.accounts,
          onClickAccount = onClickAccount,
          onClickSetUp = onClickSetUpAccounts,
        )

        BottomSpacing()
      }
    }
  }
}

@Composable
private fun HomeTitle(budgetName: String?, modifier: Modifier = Modifier) {
  Column(modifier = modifier) {
    Text(text = Strings.homeTitle, maxLines = 1, overflow = Ellipsis)
    if (budgetName != null) {
      Text(
        text = budgetName,
        style = MaterialTheme.typography.bodySmall,
        color = colors.pageTextSubdued,
        maxLines = 1,
        overflow = Ellipsis,
      )
    }
  }
}

private class HomeStateProvider :
  ColoredParameterProvider<HomeState>(
    HomeState(
      budgetName = "Household budget",
      accounts = AccountsCardState.Loaded(PREVIEW_ACCOUNTS),
    ),
    HomeState(budgetName = null, accounts = Empty),
  )

@PortraitPreview
@Composable
private fun PreviewHomeScaffold(
  @PreviewParameter(HomeStateProvider::class) params: ColoredParams<HomeState>
) =
  PreviewWithColoredParams(params) {
    HomeScaffold(state = this, onClickAccount = {}, onClickSetUpAccounts = {})
  }
