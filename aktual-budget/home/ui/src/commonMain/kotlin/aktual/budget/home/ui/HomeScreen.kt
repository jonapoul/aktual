package aktual.budget.home.ui

import aktual.budget.home.vm.AccountsCardState
import aktual.budget.home.vm.HomeState
import aktual.budget.home.vm.HomeViewModel
import aktual.core.l10n.Strings
import aktual.core.nav.BankSyncNavigator
import aktual.core.nav.BankSyncSettingsNavigator
import aktual.core.nav.EditScheduleNavigator
import aktual.core.nav.LinkBankAccountNavigator
import aktual.core.nav.ListSchedulesNavigator
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
import aktual.core.ui.hazedTopBar
import aktual.core.ui.hazedTopBarContent
import aktual.core.ui.hazedTopBarContentPadding
import aktual.core.ui.rememberHazedTopBarState
import aktual.core.ui.transparentTopAppBarColors
import aktual.core.ui.verticalScrollWithBar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
  bankSyncSettings: BankSyncSettingsNavigator,
  linkBankAccount: LinkBankAccountNavigator,
  schedules: ListSchedulesNavigator,
  editSchedule: EditScheduleNavigator,
  modifier: Modifier = Modifier,
  viewModel: HomeViewModel = metroViewModel(),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  HomeScaffold(
    state = state,
    onAction = { action ->
      when (action) {
        is OpenAccount -> transactions(action.id)
        OpenBankSync -> bankSync()
        is OpenBankSyncSettings -> bankSyncSettings(action.id)
        is LinkBankAccount -> linkBankAccount(action.id)
        ReviewUncategorised -> transactions.uncategorised()
        is OpenSchedule -> editSchedule(action.id)
        OpenSchedules -> schedules()
      }
    },
    modifier = modifier,
  )
}

@Composable
private fun HomeScaffold(
  state: HomeState,
  onAction: HomeActionHandler,
  modifier: Modifier = Modifier,
) {
  val hazeState = rememberHazedTopBarState()
  val scrollState = rememberScrollState()

  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        modifier = Modifier.hazedTopBar(hazeState, scrollOffset = { scrollState.value.toFloat() }),
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
            .hazedTopBarContent(hazeState, innerPadding)
            .verticalScrollWithBar(scrollState)
            .padding(hazedTopBarContentPadding(hazeState, innerPadding))
            .padding(Dimens.VeryLarge),
        verticalArrangement = Arrangement.spacedBy(Dimens.VeryLarge),
      ) {
        ThisMonthCard(state = state.thisMonth)

        AttentionCard(state = state.attention, onAction = onAction)

        UpcomingCard(state = state.upcoming, onAction = onAction)

        AccountsCard(state = state.accounts, onAction = onAction)

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
      thisMonth = PREVIEW_THIS_MONTH,
      attention = PREVIEW_ATTENTION,
      upcoming = PREVIEW_UPCOMING,
      accounts = AccountsCardState.Loaded(PREVIEW_ACCOUNTS),
    ),
    HomeState(budgetName = null, upcoming = Empty, accounts = Empty),
  )

@PortraitPreview
@Composable
private fun PreviewHomeScaffold(
  @PreviewParameter(HomeStateProvider::class) params: ColoredParams<HomeState>
) =
  PreviewWithColoredParams(params) {
    HomeScaffold(state = this, onAction = {})
  }
