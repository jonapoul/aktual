package aktual.budget.transactions.ui

import aktual.budget.banksync.ui.showBankSyncSummary
import aktual.budget.db.dao.ClearedBalances
import aktual.budget.model.AccountSpec
import aktual.budget.model.Amount
import aktual.budget.model.TransactionId
import aktual.budget.model.TransactionsDensity
import aktual.budget.model.TransactionsSpec
import aktual.budget.tags.domain.TagLabel
import aktual.budget.transactions.vm.AccountViewOptions
import aktual.budget.transactions.vm.LoadedAccount
import aktual.budget.transactions.vm.LoadedAccount.SpecificAccount
import aktual.budget.transactions.vm.Transaction
import aktual.budget.transactions.vm.TransactionsViewModel
import aktual.core.l10n.Strings
import aktual.core.nav.BackNavigator
import aktual.core.nav.DeletedAccountNavigator
import aktual.core.nav.EditTransactionNavigator
import aktual.core.ui.AktualAlertDialog
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.ColorParams
import aktual.core.ui.DesktopPreview
import aktual.core.ui.HazedPullToRefreshBox
import aktual.core.ui.LandscapePreview
import aktual.core.ui.LocalSnackbars
import aktual.core.ui.PageBackground
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColorParams
import aktual.core.ui.TabletPreview
import aktual.core.ui.hazedTopBarContentPadding
import aktual.core.ui.rememberHazedTopBarState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.util.fastFirstOrNull
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.flow.Flow

@Composable
fun TransactionsScreen(
  back: BackNavigator,
  accountDeleted: DeletedAccountNavigator,
  openTransaction: EditTransactionNavigator,
  spec: TransactionsSpec,
  isRoot: Boolean = false,
  viewModel: TransactionsViewModel = metroViewModel(spec),
) {
  val loadedAccount by viewModel.loadedAccount.collectAsStateWithLifecycle()
  val density by viewModel.density.collectAsStateWithLifecycle()
  val balance by viewModel.balance.collectAsStateWithLifecycle()
  val canBankSync by viewModel.canBankSync.collectAsStateWithLifecycle()
  val isBankSyncing by viewModel.isBankSyncing.collectAsStateWithLifecycle()
  val toggledSplits by viewModel.toggledSplits.collectAsStateWithLifecycle()
  val expandSplits by viewModel.expandSplits.collectAsStateWithLifecycle()
  val alternateRowColours by viewModel.alternateRowColours.collectAsStateWithLifecycle()
  val accountOptions by viewModel.accountOptions.collectAsStateWithLifecycle()
  val showRunningBalance by viewModel.showRunningBalance.collectAsStateWithLifecycle()
  val clearedBalances by viewModel.clearedBalances.collectAsStateWithLifecycle()
  val tags by viewModel.tags.collectAsStateWithLifecycle()
  val otherAccountNames by viewModel.otherAccountNames.collectAsStateWithLifecycle()
  val snackbars = LocalSnackbars.current

  LaunchedEffect(viewModel) {
    viewModel.bankSyncFinished.collect { summary ->
      snackbars.show { showBankSyncSummary(summary) }
    }
  }

  val deleted = Strings.transactionsDeleteAccountDone
  val deleteFailed = Strings.transactionsDeleteAccountFailed
  val renameFailed = Strings.transactionsRenameAccountFailed
  var showLoginKept by rememberSaveable { mutableStateOf(false) }
  LaunchedEffect(viewModel) {
    viewModel.events.collect { event ->
      when (event) {
        AccountDeleted -> snackbars.show(message = deleted).also { accountDeleted() }
        AccountDeletedBankLoginKept -> showLoginKept = true
        DeleteAccountFailed -> snackbars.show(message = deleteFailed)
        RenameAccountFailed -> snackbars.show(message = renameFailed)
      }
    }
  }

  TransactionsScaffold(
    pagingData = viewModel.pagingData,
    loadedAccount = loadedAccount,
    density = density,
    balance = balance,
    showBalance = viewModel.showBalance,
    showRunningBalance = showRunningBalance,
    clearedBalances = clearedBalances,
    isRoot = isRoot,
    canBankSync = canBankSync,
    isBankSyncing = isBankSyncing,
    toggledSplits = toggledSplits,
    expandSplits = expandSplits,
    splitsPinnedOpen = viewModel.splitsPinnedOpen,
    alternateRowColours = alternateRowColours,
    accountOptions = accountOptions,
    tags = tags,
    otherAccountNames = otherAccountNames,
    onAction = { action ->
      when (action) {
        NavBack -> back()
        BankSync -> viewModel.bankSync()
        DeleteAccount -> viewModel.deleteAccount()
        is RenameAccount -> viewModel.renameAccount(action.name)
        is SetDensity -> viewModel.setDensity(action.density)
        is SetAlternateRowColours -> viewModel.setAlternateRowColours(action.enabled)
        is SetExpandSplits -> viewModel.setExpandSplits(action.enabled)
        is SetAccountOption -> viewModel.setAccountOption(action.option, action.enabled)
        is ToggleSplit -> viewModel.toggleSplit(action.id)
        is OpenTransaction -> openTransaction(action.id)
        CreateTransaction ->
          openTransaction.create(
            account = (spec.accountSpec as? AccountSpec.SpecificAccount)?.id,
            category = (spec.categorySpec as? SpecificCategory)?.id,
          )
      }
    },
  )

  if (showLoginKept) {
    BankLoginKeptDialog(
      onDismiss = {
        showLoginKept = false
        accountDeleted()
      },
    )
  }
}

@Composable
private fun BankLoginKeptDialog(onDismiss: () -> Unit) =
  AktualAlertDialog(
    title = Strings.transactionsDeleteAccountLoginKeptTitle,
    highlight = colors.warningText,
    onDismissRequest = onDismiss,
    buttons = {
      TextButton(onClick = onDismiss) { Text(Strings.transactionsDeleteAccountLoginKeptDismiss) }
    },
    content = { Text(Strings.transactionsDeleteAccountLoginKeptMessage) },
  )

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
  showRunningBalance: Boolean = true,
  clearedBalances: ClearedBalances? = null,
  canBankSync: Boolean = false,
  isBankSyncing: Boolean = false,
  toggledSplits: ImmutableSet<TransactionId> = persistentSetOf(),
  expandSplits: Boolean = false,
  splitsPinnedOpen: Boolean = false,
  alternateRowColours: Boolean = false,
  accountOptions: AccountViewOptions? = null,
  tags: ImmutableList<TagLabel> = persistentListOf(),
  otherAccountNames: ImmutableSet<String> = persistentSetOf(),
) {
  val hazeState = rememberHazedTopBarState()
  val listState = rememberLazyListState()
  val pagingItems = pagingData.collectAsLazyPagingItems()
  var showViewOptions by remember { mutableStateOf(false) }
  var topBarHeight by remember { mutableIntStateOf(0) }
  val year by rememberVisibleYear(listState, pagingItems) { topBarHeight }

  WithLedgerDimens(
    density = density,
    showBalance = showBalance && showRunningBalance,
    showAccount = loadedAccount !is SpecificAccount,
  ) {
    Scaffold(
      topBar = {
        Column(modifier = Modifier.onSizeChanged { topBarHeight = it.height }) {
          TransactionsTitleBar(
            hazeState = hazeState,
            listState = listState,
            loadedAccount = loadedAccount,
            isRoot = isRoot,
            otherAccountNames = otherAccountNames,
            onAction = onAction,
            onOpenViewOptions = { showViewOptions = true },
          )

          // Dense rows have their own balance column, so the strip only shows for the totals
          if (showBalance && (density != Dense || clearedBalances != null)) {
            BalanceStrip(balance, clearedBalances = clearedBalances)
          }
          if (density == Dense) LedgerHeader()
          year?.let { YearDivider(it) }
        }
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
            toggledSplits = toggledSplits,
            expandSplits = expandSplits,
            splitsPinnedOpen = splitsPinnedOpen,
            alternateRowColours = alternateRowColours,
            tags = tags,
            onAction = onAction,
          )
        }
      }
    }
  }

  if (showViewOptions) {
    ViewOptionsSheet(
      options =
        ViewOptions(
          density = density,
          alternateRowColours = alternateRowColours,
          expandSplits = expandSplits,
          account = accountOptions,
        ),
      onAction = onAction,
      onDismiss = { showViewOptions = false },
    )
  }
}

// The year of the topmost transaction that isn't hidden behind the top bar. It's pinned under the
// bar, where the list's own year dividers scroll up to meet it
@Composable
private fun rememberVisibleYear(
  listState: LazyListState,
  pagingItems: LazyPagingItems<Transaction>,
  topBarHeight: () -> Int,
): State<Int?> =
  remember(listState, pagingItems) {
    derivedStateOf {
      // The list's first item is the spacer under the top bar, so transactions are one further on
      val topItem =
        listState.layoutInfo.visibleItemsInfo.fastFirstOrNull {
          it.index > 0 && it.offset + it.size > topBarHeight()
        }
      topItem?.let { pagingItems.itemSnapshotList.getOrNull(it.index - 1)?.date?.year }
    }
  }

@Composable
@PortraitPreview
@LandscapePreview
@TabletPreview
@DesktopPreview
private fun PreviewTransactionsScaffold(
  @PreviewParameter(DensityProvider::class) params: ColorParams<TransactionsDensity>,
) =
  PreviewWithColorParams(params) {
    TransactionsScaffold(
      pagingData = previewPagingData(PREVIEW_TRANSACTIONS),
      loadedAccount = AllAccounts,
      density = this,
      balance = PREVIEW_BALANCE,
      isRoot = true,
      toggledSplits = persistentSetOf(TRANSACTION_SPLIT.id),
      onAction = {},
    )
  }
