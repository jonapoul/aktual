package aktual.budget.transactions.ui

import aktual.budget.accounts.components.EditAccountSheet
import aktual.budget.banksync.components.showBankSyncSummary
import aktual.budget.banksync.domain.BankSyncSummary
import aktual.budget.db.dao.ClearedBalances
import aktual.budget.model.AccountId
import aktual.budget.model.AccountSpec
import aktual.budget.model.Amount
import aktual.budget.model.TagSpec
import aktual.budget.model.TransactionId
import aktual.budget.model.TransactionSearch
import aktual.budget.model.TransactionsDensity
import aktual.budget.model.TransactionsSpec
import aktual.budget.rules.domain.ConditionEntities
import aktual.budget.tags.domain.TagLabel
import aktual.budget.transactions.ui.edit.ReconciledDialog
import aktual.budget.transactions.vm.AccountChoice
import aktual.budget.transactions.vm.AccountViewOptions
import aktual.budget.transactions.vm.LoadedAccount
import aktual.budget.transactions.vm.LoadedAccount.SpecificAccount
import aktual.budget.transactions.vm.ReconcileStart
import aktual.budget.transactions.vm.Reconciling
import aktual.budget.transactions.vm.SavedFiltersState
import aktual.budget.transactions.vm.Transaction
import aktual.budget.transactions.vm.TransactionsEvent
import aktual.budget.transactions.vm.TransactionsViewModel
import aktual.core.l10n.Res
import aktual.core.l10n.Strings
import aktual.core.l10n.transactions_filter_conditions_taken
import aktual.core.l10n.transactions_filter_name_taken
import aktual.core.nav.BackNavigator
import aktual.core.nav.DeletedAccountNavigator
import aktual.core.nav.EditTransactionNavigator
import aktual.core.nav.SwitchAccountNavigator
import aktual.core.nav.TransactionsNavigator
import aktual.core.ui.AktualAlertDialog
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.BackHandler
import aktual.core.ui.ColorParams
import aktual.core.ui.DesktopPreview
import aktual.core.ui.DialogTextButton
import aktual.core.ui.HazedPullToRefreshBox
import aktual.core.ui.LandscapePreview
import aktual.core.ui.LocalSnackbars
import aktual.core.ui.OnAction
import aktual.core.ui.PageBackground
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColorParams
import aktual.core.ui.Snackbars
import aktual.core.ui.TabletPreview
import aktual.core.ui.hazedTopBarContentPadding
import aktual.core.ui.rememberHazedTopBarState
import aktual.core.ui.showErrorSnackbar
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
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
import org.jetbrains.compose.resources.getString

@Composable
fun TransactionsScreen(
  back: BackNavigator,
  accountDeleted: DeletedAccountNavigator,
  openTransaction: EditTransactionNavigator,
  openTag: TransactionsNavigator,
  switchAccount: SwitchAccountNavigator,
  spec: TransactionsSpec,
  isRoot: Boolean = false,
  viewModel: TransactionsViewModel = metroViewModel(spec),
) {
  val loadedAccount by viewModel.loadedAccount.collectAsStateWithLifecycle()
  val density by viewModel.density.collectAsStateWithLifecycle()
  val balance by viewModel.balance.collectAsStateWithLifecycle()
  val showBalance by viewModel.showBalance.collectAsStateWithLifecycle()
  val canBankSync by viewModel.canBankSync.collectAsStateWithLifecycle()
  val isBankSyncing by viewModel.isBankSyncing.collectAsStateWithLifecycle()
  val toggledSplits by viewModel.toggledSplits.collectAsStateWithLifecycle()
  val expandSplits by viewModel.expandSplits.collectAsStateWithLifecycle()
  val alternateRowColours by viewModel.alternateRowColours.collectAsStateWithLifecycle()
  val accountOptions by viewModel.accountOptions.collectAsStateWithLifecycle()
  val accountChoices by viewModel.accountChoices.collectAsStateWithLifecycle()
  val showRunningBalance by viewModel.showRunningBalance.collectAsStateWithLifecycle()
  val clearedBalances by viewModel.clearedBalances.collectAsStateWithLifecycle()
  val tags by viewModel.tags.collectAsStateWithLifecycle()
  val reconciling by viewModel.reconciling.collectAsStateWithLifecycle()
  val reconcileStart by viewModel.reconcileStart.collectAsStateWithLifecycle()
  val search by viewModel.search.collectAsStateWithLifecycle()
  val conditionEntities by viewModel.conditionEntities.collectAsStateWithLifecycle()
  val savedFilters by viewModel.savedFiltersState.collectAsStateWithLifecycle()
  val snackbars = LocalSnackbars.current

  BankSyncSnackbars(viewModel.bankSyncFinished, snackbars)

  val adjustmentNotes = Strings.transactionsReconcileAdjustmentNotes
  var showLoginKept by rememberSaveable { mutableStateOf(false) }
  var confirmUnlock by remember { mutableStateOf<TransactionId?>(null) }
  var editAccount by remember { mutableStateOf<AccountId?>(null) }
  TransactionsEvents(
    events = viewModel.events,
    snackbars = snackbars,
    onAccountDelete = accountDeleted::invoke,
    onBankLoginKeep = { showLoginKept = true },
  )

  TransactionsScaffold(
    pagingData = viewModel.pagingData,
    loadedAccount = loadedAccount,
    density = density,
    balance = balance,
    showBalance = showBalance,
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
    accountChoices = accountChoices,
    tags = tags,
    reconciling = reconciling,
    reconcileStart = reconcileStart,
    search = search.takeIf { viewModel.canSearch },
    conditionEntities = conditionEntities,
    savedFilters = savedFilters,
    onAction = { action ->
      when (action) {
        NavBack -> back()
        is EditAccount -> editAccount = action.id
        is SwitchAccount -> switchAccount(action.id)
        is OpenTransaction -> openTransaction(action.id)
        is OpenTag -> if (spec.tagSpec != TagSpec.SpecificTag(action.id)) openTag(action.id)
        CreateTransaction ->
          openTransaction.create(
            account = (spec.accountSpec as? AccountSpec.SpecificAccount)?.id,
            category = (spec.categorySpec as? SpecificCategory)?.id,
          )
        is ToggleCleared ->
          if (action.reconciled) {
            confirmUnlock = action.id
          } else {
            viewModel.setCleared(action.id, !action.cleared)
          }
        CreateReconcileAdjustment -> viewModel.createReconcileAdjustment(adjustmentNotes)
        is ViewModelAction -> viewModel.handle(action)
      }
    },
  )

  editAccount?.let { id -> EditAccountSheet(id = id, onDismiss = { editAccount = null }) }

  confirmUnlock?.let { id ->
    ReconciledDialog(
      message = Strings.transactionReconciledUnlock,
      onConfirm = {
        confirmUnlock = null
        viewModel.unlockTransaction(id)
      },
      onCancel = { confirmUnlock = null },
    )
  }

  if (showLoginKept) {
    BankLoginKeptDialog(
      onDismiss = {
        showLoginKept = false
        accountDeleted()
      },
    )
  }
}

private fun TransactionsViewModel.handle(action: ViewModelAction) {
  when (action) {
    Action.BankSync -> bankSync()
    Action.DeleteAccount -> deleteAccount()
    is Action.SetDensity -> setDensity(action.density)
    is Action.SetAlternateRowColours -> setAlternateRowColours(action.enabled)
    is Action.SetExpandSplits -> setExpandSplits(action.enabled)
    is Action.SetAccountOption -> setAccountOption(action.option, action.enabled)
    is Action.ToggleSplit -> toggleSplit(action.id)
    is Action.StartReconciling -> startReconciling(action.target)
    Action.FinishReconciling -> finishReconciling()
    is Action.SetQuery -> setQuery(action.text)
    is Action.AddCondition -> addCondition(action.condition)
    is Action.EditCondition -> editCondition(action.index, action.condition)
    is Action.RemoveCondition -> removeCondition(action.index)
    is Action.SetConditionsOp -> setConditionsOp(action.op)
    Action.ClearFilters -> clearFilters()
    Action.ClearConditions -> clearConditions()
    is Action.ApplySavedFilter -> applySavedFilter(action.filter)
    is Action.SaveFilter -> saveFilter(action.name)
    Action.UpdateSavedFilter -> updateSavedFilter()
    is Action.RenameSavedFilter -> renameSavedFilter(action.name)
    Action.DeleteSavedFilter -> deleteSavedFilter()
  }
}

@Composable
private fun TransactionsEvents(
  events: Flow<TransactionsEvent>,
  snackbars: Snackbars,
  onAccountDelete: () -> Unit,
  onBankLoginKeep: () -> Unit,
) {
  val deleted = Strings.transactionsDeleteAccountDone
  val deleteFailed = Strings.transactionsDeleteAccountFailed
  val reconcileFailed = Strings.transactionsReconcileFailed
  val saveFilterFailed = Strings.transactionsFilterSaveFailed
  val currentOnAccountDelete by rememberUpdatedState(onAccountDelete)
  val currentOnBankLoginKeep by rememberUpdatedState(onBankLoginKeep)
  LaunchedEffect(events) {
    events.collect { event ->
      when (event) {
        AccountDeleted -> snackbars.show(message = deleted).also { currentOnAccountDelete() }
        AccountDeletedBankLoginKept -> currentOnBankLoginKeep()
        DeleteAccountFailed -> snackbars.show { showErrorSnackbar(deleteFailed) }
        ReconcileFailed -> snackbars.show { showErrorSnackbar(reconcileFailed) }
        SaveFilterFailed -> snackbars.show { showErrorSnackbar(saveFilterFailed) }
        is FilterNameTaken ->
          snackbars.show {
            showErrorSnackbar(getString(Res.string.transactions_filter_name_taken, event.name))
          }
        is FilterConditionsTaken ->
          snackbars.show {
            showErrorSnackbar(
              getString(Res.string.transactions_filter_conditions_taken, event.name),
            )
          }
      }
    }
  }
}

@Composable
private fun BankSyncSnackbars(finished: Flow<BankSyncSummary>, snackbars: Snackbars) {
  LaunchedEffect(finished) {
    finished.collect { summary ->
      snackbars.show { showBankSyncSummary(summary) }
    }
  }
}

@Composable
private fun BankLoginKeptDialog(onDismiss: () -> Unit) =
  AktualAlertDialog(
    title = Strings.transactionsDeleteAccountLoginKeptTitle,
    highlight = colors.warningText,
    onDismissRequest = onDismiss,
    buttons = {
      DialogTextButton(
        text = Strings.transactionsDeleteAccountLoginKeptDismiss,
        onClick = onDismiss,
      )
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
  onAction: OnAction<Action>,
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
  accountChoices: ImmutableList<AccountChoice> = persistentListOf(),
  tags: ImmutableList<TagLabel> = persistentListOf(),
  reconciling: Reconciling? = null,
  reconcileStart: ReconcileStart? = null,
  // Null for a list that can't be searched
  search: TransactionSearch? = null,
  conditionEntities: ConditionEntities? = null,
  savedFilters: SavedFiltersState = SavedFiltersState(),
) {
  // Filters outlive the screen, so a list opened with a query already set shows its field
  var isSearching by rememberSaveable { mutableStateOf(search?.text.orEmpty().isNotEmpty()) }
  val closeSearch = {
    isSearching = false
    onAction(Action.SetQuery(""))
  }
  BackHandler(enabled = isSearching, onBack = closeSearch)
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
    isReconciling = reconciling != null,
    alternateRowColours = alternateRowColours,
  ) {
    CompositionLocalProvider(LocalTagLabels provides tags) {
      Scaffold(
        topBar = {
          Column(modifier = Modifier.onSizeChanged { topBarHeight = it.height }) {
            TransactionsTitleBar(
              hazeState = hazeState,
              listState = listState,
              loadedAccount = loadedAccount,
              isRoot = isRoot,
              reconcileStart = reconcileStart,
              accountChoices = accountChoices,
              canSearch = search != null,
              isSearching = isSearching,
              query = search?.text.orEmpty(),
              onSearch = { isSearching = true },
              onCloseSearch = closeSearch,
              onAction = onAction,
              onOpenViewOptions = { showViewOptions = true },
            )

            if (search != null) {
              AnimatedVisibility(
                visible =
                  isSearching ||
                    search.conditions.conditions.isNotEmpty() ||
                    savedFilters.filters.isNotEmpty(),
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
              ) {
                TransactionFilterChips(search.conditions, conditionEntities, savedFilters, onAction)
              }
            }

            // Dense rows have their own balance column, so the strip only shows for the totals
            if (showBalance && (density != Dense || clearedBalances != null)) {
              BalanceStrip(balance, clearedBalances = clearedBalances)
            }
            reconciling?.let { ReconcilingBanner(it, onAction) }
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
              innerPadding = innerPadding,
              toggledSplits = toggledSplits,
              expandSplits = expandSplits,
              splitsPinnedOpen = splitsPinnedOpen,
              isSearched = search?.isEmpty == false,
              onAction = onAction,
            )
          }
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
      // Before the list's first layout, use the first transaction so the bar doesn't grow a frame
      // later and animate every row down
      val index = topItem?.let { it.index - 1 } ?: 0
      pagingItems.itemSnapshotList.getOrNull(index)?.date?.year
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
