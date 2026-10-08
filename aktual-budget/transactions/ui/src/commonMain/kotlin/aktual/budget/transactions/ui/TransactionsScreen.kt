package aktual.budget.transactions.ui

import aktual.budget.banksync.ui.showBankSyncSummary
import aktual.budget.model.Amount
import aktual.budget.model.TransactionId
import aktual.budget.model.TransactionsDensity
import aktual.budget.model.TransactionsSpec
import aktual.budget.transactions.vm.LoadedAccount
import aktual.budget.transactions.vm.Transaction
import aktual.budget.transactions.vm.TransactionsViewModel
import aktual.core.nav.BackNavigator
import aktual.core.nav.TransactionSettingsNavigator
import aktual.core.ui.ColoredParams
import aktual.core.ui.DesktopPreview
import aktual.core.ui.HazedPullToRefreshBox
import aktual.core.ui.LandscapePreview
import aktual.core.ui.LocalBottomSpacing
import aktual.core.ui.PageBackground
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.TabletPreview
import aktual.core.ui.bottomNavBarPadding
import aktual.core.ui.hazedTopBarContentPadding
import aktual.core.ui.rememberHazedTopBarState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.flow.Flow

@Composable
fun TransactionsScreen(
  back: BackNavigator,
  toSettings: TransactionSettingsNavigator,
  spec: TransactionsSpec,
  isRoot: Boolean = false,
  viewModel: TransactionsViewModel = metroViewModel(spec),
) {
  val loadedAccount by viewModel.loadedAccount.collectAsStateWithLifecycle()
  val density by viewModel.density.collectAsStateWithLifecycle()
  val balance by viewModel.balance.collectAsStateWithLifecycle()
  val canBankSync by viewModel.canBankSync.collectAsStateWithLifecycle()
  val isBankSyncing by viewModel.isBankSyncing.collectAsStateWithLifecycle()
  val expanded by viewModel.expanded.collectAsStateWithLifecycle()
  val alternateRowColours by viewModel.alternateRowColours.collectAsStateWithLifecycle()
  val snackbar = remember { SnackbarHostState() }

  LaunchedEffect(viewModel) {
    viewModel.bankSyncFinished.collect { summary -> snackbar.showBankSyncSummary(summary) }
  }

  TransactionsScaffold(
    pagingData = viewModel.pagingData,
    loadedAccount = loadedAccount,
    density = density,
    balance = balance,
    showBalance = viewModel.showBalance,
    isRoot = isRoot,
    canBankSync = canBankSync,
    isBankSyncing = isBankSyncing,
    expanded = expanded,
    splitsPinnedOpen = viewModel.splitsPinnedOpen,
    alternateRowColours = alternateRowColours,
    snackbarHostState = snackbar,
    onAction = { action ->
      when (action) {
        NavBack -> back()
        BankSync -> viewModel.bankSync()
        OpenSettings -> toSettings()
        is SetPrivacyMode -> viewModel.setPrivacyMode(action.isPrivacyEnabled)
        is SetDensity -> viewModel.setDensity(action.density)
        is ToggleSplit -> viewModel.toggleExpanded(action.id)
      }
    },
  )
}

@Composable
private fun metroViewModel(spec: TransactionsSpec) =
  assistedMetroViewModel<TransactionsViewModel, TransactionsViewModel.Factory>(
    key = spec.toString(),
    createViewModel = { create(spec) },
  )

@Composable
internal fun TransactionsScaffold(
  pagingData: Flow<PagingData<Transaction>>,
  loadedAccount: LoadedAccount,
  density: TransactionsDensity,
  balance: Amount?,
  isRoot: Boolean,
  onAction: ActionListener,
  showBalance: Boolean = true,
  canBankSync: Boolean = false,
  isBankSyncing: Boolean = false,
  expanded: ImmutableSet<TransactionId> = persistentSetOf(),
  splitsPinnedOpen: Boolean = false,
  alternateRowColours: Boolean = false,
  snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
  val hazeState = rememberHazedTopBarState()
  val listState = rememberLazyListState()
  val pagingItems = pagingData.collectAsLazyPagingItems()
  var showViewOptions by remember { mutableStateOf(false) }

  WithLedgerDimens(density, showBalance) {
    Scaffold(
      topBar = {
        Column {
          TransactionsTitleBar(
            hazeState = hazeState,
            listState = listState,
            loadedAccount = loadedAccount,
            isRoot = isRoot,
            onAction = onAction,
            onOpenViewOptions = { showViewOptions = true },
          )

          if (density != Dense && showBalance) BalanceStrip(balance)
          if (density == Dense) LedgerHeader()
        }
      },
      snackbarHost = {
        SnackbarHost(
          hostState = snackbarHostState,
          modifier = Modifier.padding(bottom = LocalBottomSpacing.current + bottomNavBarPadding()),
        )
      },
    ) { innerPadding ->
      Box {
        PageBackground()

        // Pulling down downloads the account's transactions from its bank
        HazedPullToRefreshBox(
          hazeState = hazeState,
          innerPadding = innerPadding,
          isRefreshing = isBankSyncing,
          onRefresh = { onAction(BankSync) },
          enabled = canBankSync,
        ) {
          Transactions(
            contentPadding = hazedTopBarContentPadding(hazeState, innerPadding = Zero),
            listState = listState,
            pagingItems = pagingItems,
            density = density,
            innerPadding = innerPadding,
            expanded = expanded,
            splitsPinnedOpen = splitsPinnedOpen,
            alternateRowColours = alternateRowColours,
            onAction = onAction,
          )
        }
      }
    }
  }

  if (showViewOptions) {
    ViewOptionsSheet(
      density = density,
      onAction = onAction,
      onDismiss = { showViewOptions = false },
    )
  }
}

@Composable
@PortraitPreview
@LandscapePreview
@TabletPreview
@DesktopPreview
private fun PreviewTransactionsScaffold(
  @PreviewParameter(DensityProvider::class) params: ColoredParams<TransactionsDensity>,
) =
  PreviewWithColoredParams(params) {
    TransactionsScaffold(
      pagingData = previewPagingData(PREVIEW_TRANSACTIONS),
      loadedAccount = AllAccounts,
      density = this,
      balance = PREVIEW_BALANCE,
      isRoot = true,
      expanded = persistentSetOf(TRANSACTION_SPLIT.id),
      onAction = {},
    )
  }
