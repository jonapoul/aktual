package aktual.budget.transactions.ui

import aktual.budget.model.Amount
import aktual.budget.model.TransactionsDensity
import aktual.budget.model.TransactionsSpec
import aktual.budget.transactions.vm.LoadedAccount
import aktual.budget.transactions.vm.Transaction
import aktual.budget.transactions.vm.TransactionsViewModel
import aktual.core.nav.BackNavigator
import aktual.core.ui.ColoredParams
import aktual.core.ui.DesktopPreview
import aktual.core.ui.LandscapePreview
import aktual.core.ui.PageBackground
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.TabletPreview
import aktual.core.ui.hazedTopBarContent
import aktual.core.ui.hazedTopBarContentPadding
import aktual.core.ui.rememberHazedTopBarState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
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
import kotlinx.coroutines.flow.Flow

@Composable
fun TransactionsScreen(
  back: BackNavigator,
  spec: TransactionsSpec,
  isRoot: Boolean = false,
  viewModel: TransactionsViewModel = metroViewModel(spec),
) {
  val loadedAccount by viewModel.loadedAccount.collectAsStateWithLifecycle()
  val density by viewModel.density.collectAsStateWithLifecycle()
  val balance by viewModel.balance.collectAsStateWithLifecycle()

  TransactionsScaffold(
    pagingData = viewModel.pagingData,
    loadedAccount = loadedAccount,
    density = density,
    balance = balance,
    isRoot = isRoot,
    onAction = { action ->
      when (action) {
        NavBack -> back()
        is SetPrivacyMode -> viewModel.setPrivacyMode(action.isPrivacyEnabled)
        is SetDensity -> viewModel.setDensity(action.density)
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
) {
  val hazeState = rememberHazedTopBarState()
  val listState = rememberLazyListState()
  val pagingItems = pagingData.collectAsLazyPagingItems()
  var showViewOptions by remember { mutableStateOf(false) }

  WithLedgerDimens(density) {
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

          if (density != Dense) BalanceStrip(balance)
        }
      }
    ) { innerPadding ->
      Box {
        PageBackground()
        Transactions(
          modifier = Modifier.hazedTopBarContent(hazeState, innerPadding),
          contentPadding = hazedTopBarContentPadding(hazeState, innerPadding = Zero),
          listState = listState,
          pagingItems = pagingItems,
          density = density,
          innerPadding = innerPadding,
        )
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
  @PreviewParameter(DensityProvider::class) params: ColoredParams<TransactionsDensity>
) =
  PreviewWithColors(params.colors) {
    TransactionsScaffold(
      pagingData = previewPagingData(PREVIEW_TRANSACTIONS),
      loadedAccount = LoadedAccount.AllAccounts,
      density = params.data,
      balance = PREVIEW_BALANCE,
      isRoot = true,
      onAction = {},
    )
  }
