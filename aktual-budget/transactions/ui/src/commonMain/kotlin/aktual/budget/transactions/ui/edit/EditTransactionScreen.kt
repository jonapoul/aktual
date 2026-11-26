package aktual.budget.transactions.ui.edit

import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.CategoryId
import aktual.budget.model.PayeeId
import aktual.budget.transactions.domain.SplitPart
import aktual.budget.transactions.domain.TransactionFields
import aktual.budget.transactions.ui.TRANSACTION_1
import aktual.budget.transactions.ui.TRANSACTION_OFF_BUDGET
import aktual.budget.transactions.ui.TRANSACTION_SPLIT_UNBALANCED
import aktual.budget.transactions.ui.TRANSACTION_TRANSFER
import aktual.budget.transactions.ui.color
import aktual.budget.transactions.ui.tabularFigures
import aktual.budget.transactions.vm.Transaction
import aktual.budget.transactions.vm.edit.EditTransactionError
import aktual.budget.transactions.vm.edit.EditTransactionEvent
import aktual.budget.transactions.vm.edit.EditTransactionEvent.MergedPayees
import aktual.budget.transactions.vm.edit.EditTransactionState
import aktual.budget.transactions.vm.edit.EditTransactionTarget
import aktual.budget.transactions.vm.edit.EditTransactionViewModel
import aktual.budget.transactions.vm.edit.SplitPartForm
import aktual.budget.transactions.vm.edit.TransactionDetails
import aktual.budget.transactions.vm.edit.TransactionEditMode
import aktual.budget.transactions.vm.edit.TransactionForm
import aktual.budget.transactions.vm.edit.TransactionOptions
import aktual.core.icons.AktualIcons
import aktual.core.icons.CheveronLeft
import aktual.core.icons.CheveronRight
import aktual.core.icons.DotsHorizontalTriple
import aktual.core.icons.Trash
import aktual.core.l10n.Res
import aktual.core.l10n.Strings
import aktual.core.l10n.transaction_error_merging_payees
import aktual.core.l10n.transaction_location_already_saved
import aktual.core.l10n.transaction_location_denied
import aktual.core.l10n.transaction_location_none_nearby
import aktual.core.l10n.transaction_location_save_failed
import aktual.core.l10n.transaction_location_saved
import aktual.core.l10n.transaction_location_saved_view
import aktual.core.l10n.transaction_location_unavailable
import aktual.core.l10n.transaction_unused_payees_merged
import aktual.core.l10n.transaction_unused_payees_undo
import aktual.core.nav.BackNavigator
import aktual.core.nav.ListPayeeLocationsNavigator
import aktual.core.ui.AktualDropdownMenu
import aktual.core.ui.AktualDropdownMenuItem
import aktual.core.ui.AktualIconButton
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.letterSpacings
import aktual.core.ui.AktualTheme.textSizes
import aktual.core.ui.BackHandler
import aktual.core.ui.BackOrDiscardButton
import aktual.core.ui.BottomSpacing
import aktual.core.ui.CardRow
import aktual.core.ui.ColorParams
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.EditFabs
import aktual.core.ui.FailureAction
import aktual.core.ui.FailureScreen
import aktual.core.ui.LocalSnackbars
import aktual.core.ui.OnAction
import aktual.core.ui.PageBackground
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColorParams
import aktual.core.ui.Snackbars
import aktual.core.ui.formattedText
import aktual.core.ui.hazedTopBar
import aktual.core.ui.hazedTopBarContent
import aktual.core.ui.hazedTopBarContentPadding
import aktual.core.ui.isScrollingUp
import aktual.core.ui.rememberHazedTopBarState
import aktual.core.ui.showErrorSnackbar
import aktual.core.ui.showSuccessSnackbar
import aktual.core.ui.showWarningSnackbar
import aktual.core.ui.transparentTopAppBarColors
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import org.jetbrains.compose.resources.getString

@Composable
internal fun EditTransactionScreen(
  target: EditTransactionTarget,
  back: BackNavigator,
  toBudget: () -> Unit,
  toLocations: ListPayeeLocationsNavigator,
  modifier: Modifier = Modifier,
  viewModel: EditTransactionViewModel = editTransactionViewModel(target),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  val error by viewModel.error.collectAsStateWithLifecycle()
  val unusedPayees by viewModel.unusedPayees.collectAsStateWithLifecycle()
  val fieldState = remember { EditFieldState() }
  val snackbars = LocalSnackbars.current

  LaunchedEffect(viewModel) {
    viewModel.events.collect { event ->
      onEvent(viewModel, event, back, fieldState, snackbars, toLocations)
    }
  }

  val withLocation =
    rememberWithLocation(
      needsPermission = { viewModel.needsLocationPermission },
      onDeny = {
        snackbars.show { showWarningSnackbar(getString(Res.string.transaction_location_denied)) }
      },
    )

  EditTransactionScaffold(
    modifier = modifier,
    state = state,
    error = error,
    fieldState = fieldState,
    onAction = { action ->
      when (action) {
        NavigateBack -> back()
        DiscardChanges -> viewModel.discard()
        RevertChanges -> viewModel.revert()
        SaveTransaction -> viewModel.save()
        DeleteTransaction -> viewModel.delete()
        UnlockReconciled -> viewModel.unlockReconciled()
        DismissError -> viewModel.dismissError()
        CommitAmount -> viewModel.commitAmount()
        GoToBudget -> toBudget()
        ToggleHiddenCategories -> viewModel.toggleHiddenCategories()
        is PickingCategory -> viewModel.setPickingCategory(action.isPicking)
        is SetSyncTransferDate -> viewModel.setSyncTransferDate(action.enabled)
        FindNearbyPayees -> withLocation(viewModel::findNearbyPayees)
        SavePayeeLocation -> withLocation(viewModel::saveLocation)
        is FieldAction -> viewModel.onFieldAction(action)
        is SplitAction -> viewModel.onSplitAction(action)
      }
    },
  )

  unusedPayees?.let { unused ->
    UnusedPayeesDialog(
      unused = unused,
      isWorking = (state as? Loaded)?.isWorking == true,
      onMerge = viewModel::mergeUnusedPayees,
      onKeep = viewModel::keepUnusedPayees,
    )
  }
}

private fun onEvent(
  viewModel: EditTransactionViewModel,
  event: EditTransactionEvent,
  back: BackNavigator,
  fieldState: EditFieldState,
  snackbars: Snackbars,
  toLocations: ListPayeeLocationsNavigator,
) {
  when (event) {
    Deleted,
    Saved,
    Discarded -> back()
    is MergedPayees -> {
      snackbars.show { showMerged(event, onUndo = viewModel::undoMerge) }
      back()
    }
    MergePayeesFailed -> {
      snackbars.show { showErrorSnackbar(getString(Res.string.transaction_error_merging_payees)) }
      back()
    }
    NearbyPayeesFound -> fieldState.edit(Payee)
    is LocationEvent -> snackbars.show { showLocationEvent(event, toLocations) }
  }
}

private fun EditTransactionViewModel.onFieldAction(action: FieldAction) =
  when (action) {
    is SetAmount -> setAmount(action.amount)
    is SetPayee -> setPayee(action.id)
    is CreatePayee -> createPayee(action.name)
    is SetCategory -> setCategory(action.id)
    is SetAccount -> setAccount(action.id)
    is SetDate -> setDate(action.date)
    is SetNotes -> setNotes(action.notes)
    is SetCleared -> setCleared(action.cleared)
    is CreateTag -> createTag(action.tag)
    is SetSchedule -> setSchedule(action.id)
  }

private fun EditTransactionViewModel.onSplitAction(action: SplitAction) =
  when (action) {
    SplitTransaction -> split()
    AddSplit -> addSplit()
    is RemoveSplit -> removeSplit(action.id)
    is FillRemaining -> fillRemaining(action.id)
    is SetPartAmount -> setPartAmount(action.id, action.amount)
    is SetPartPayee -> setPartPayee(action.id, action.payee)
    is CreatePartPayee -> createPartPayee(action.id, action.name)
    is SetPartCategory -> setPartCategory(action.id, action.category)
    is SetPartNotes -> setPartNotes(action.id, action.notes)
  }

private suspend fun SnackbarHostState.showLocationEvent(
  event: EditTransactionEvent.LocationEvent,
  toLocations: ListPayeeLocationsNavigator,
) {
  when (event) {
    NoNearbyPayees -> showSnackbar(getString(Res.string.transaction_location_none_nearby))
    LocationUnavailable ->
      showWarningSnackbar(getString(Res.string.transaction_location_unavailable))
    LocationSaveFailed -> showErrorSnackbar(getString(Res.string.transaction_location_save_failed))
    is LocationSaved -> {
      val result =
        showSuccessSnackbar(
          message = getString(Res.string.transaction_location_saved, event.payee),
          actionLabel = getString(Res.string.transaction_location_saved_view),
          duration = SnackbarDuration.Long,
        )
      if (result == ActionPerformed) toLocations()
    }
    is LocationAlreadySaved ->
      showSnackbar(getString(Res.string.transaction_location_already_saved, event.payee))
    NearbyPayeesFound -> Unit
  }
}

private suspend fun SnackbarHostState.showMerged(
  event: MergedPayees,
  onUndo: (MergedPayees) -> Unit,
) {
  val result =
    showSnackbar(
      message = getString(Res.string.transaction_unused_payees_merged, event.payees.target.name),
      actionLabel = getString(Res.string.transaction_unused_payees_undo),
      duration = SnackbarDuration.Long,
    )
  if (result == ActionPerformed) onUndo(event)
}

@Composable
private fun editTransactionViewModel(target: EditTransactionTarget) =
  assistedMetroViewModel<EditTransactionViewModel, EditTransactionViewModel.Factory>(
    key = target.toString(),
    createViewModel = { extras -> create(target, extras.createSavedStateHandle()) },
  )

@Composable
private fun EditTransactionScaffold(
  state: EditTransactionState,
  error: EditTransactionError?,
  onAction: OnAction<EditTransactionAction>,
  modifier: Modifier = Modifier,
  fieldState: EditFieldState = remember { EditFieldState() },
) {
  val loaded = state as? Loaded
  val form = loaded?.mode?.form
  var dialog by remember { mutableStateOf<TransactionDialog?>(null) }
  val isAmountValid = !fieldState.isTypingAmount || fieldState.amountText.isValidAmount()

  // Backing out checks first if that would lose any changes
  fun onBack() {
    when {
      loaded?.isWorking == true -> Unit
      form?.hasChanges == true -> dialog = ConfirmDiscard
      else -> onAction(NavigateBack)
    }
  }
  BackHandler(enabled = form?.hasChanges == true) { onBack() }

  // A conflict that turns up while a save is being confirmed has to be confirmed first
  val isConflicting = loaded?.changedElsewhere == Conflicting
  SideEffect(isConflicting) {
    if (
      isConflicting && (dialog == ConfirmReconciledSave || dialog == ConfirmReconciledTransferSave)
    ) {
      dialog = ConfirmOverwrite
    }
  }

  val hazeState = rememberHazedTopBarState()
  val scrollState = rememberScrollState()

  fun onSave() {
    val confirm =
      if (loaded?.changedElsewhere == Conflicting) {
        ConfirmOverwrite
      } else {
        saveDialog(form, loaded?.saved)
      }
    if (confirm == null) onAction(SaveTransaction) else dialog = confirm
  }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      EditTransactionTopBar(
        modifier = Modifier.hazedTopBar(hazeState, scrollState),
        state = state,
        onBack = { onBack() },
        onDelete = { dialog = deleteDialog(loaded?.saved) },
      )
    },
  ) { innerPadding ->
    Box(modifier = Modifier.fillMaxSize()) {
      PageBackground()

      when (state) {
        Loading -> ShimmerTransaction(modifier = Modifier.padding(innerPadding))

        is Failure -> LoadFailure(state, onAction, Modifier.padding(innerPadding))

        is Empty -> NothingToCreateWith(state, onAction, Modifier.padding(innerPadding))

        is Loaded ->
          EditTransactionForm(
            modifier = Modifier.hazedTopBarContent(hazeState, innerPadding),
            form = state.mode.form,
            fieldState = fieldState,
            balance = state.saved?.transaction?.balance,
            changedElsewhere = state.changedElsewhere,
            onUnlock = { dialog = ConfirmReconciledUnlock },
            onAction = onAction,
            scrollState = scrollState,
            contentPadding = hazedTopBarContentPadding(hazeState, innerPadding),
          )
      }

      if (loaded != null) {
        SaveRevertFabs(
          state = loaded,
          canSave = loaded.canSave && isAmountValid,
          // The keypad covers the bottom of the screen while typing an amount
          isVisible = !fieldState.isTypingAmount,
          scrollState = scrollState,
          onSave = { onSave() },
          onRevert = { onAction(RevertChanges) },
        )
      }

      if (form != null) {
        EditTransactionSheet(
          active = fieldState.active,
          form = form,
          onAction = onAction,
          onDismiss = { fieldState.close() },
        )
      }

      EditTransactionDialogs(
        dialog = dialog,
        onAction = onAction,
        onShow = { dialog = it },
        // The reconciled checks still come after it
        afterOverwrite = { saveDialog(form, loaded?.saved) },
      )

      if (error != null) {
        EditTransactionErrorDialog(error = error, onDismiss = { onAction(DismissError) })
      }
    }
  }
}

@Composable
private fun EditTransactionTopBar(
  state: EditTransactionState,
  onBack: () -> Unit,
  onDelete: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val loaded = state as? Loaded
  val isCreating = loaded?.mode is Create || state is Empty
  TopAppBar(
    modifier = modifier,
    colors = colors.transparentTopAppBarColors(),
    navigationIcon = {
      BackOrDiscardButton(
        hasChanges = loaded?.mode?.form?.hasChanges == true,
        discardText = Strings.transactionDiscardConfirm,
        onBack = onBack,
        onDiscard = onBack,
      )
    },
    title = { if (isCreating) Text(text = Strings.transactionCreateTitle) },
    actions = {
      if (loaded != null) TopBarActions(state = loaded, onDelete = onDelete)
    },
  )
}

// As upstream, changing a reconciled transaction is checked first, and so is changing a transfer
// whose other side is reconciled
private fun saveDialog(form: TransactionForm?, saved: TransactionDetails?): TransactionDialog? =
  when {
    form?.draft?.reconciled == true -> ConfirmReconciledSave
    saved?.hasReconciledTransfer == true -> ConfirmReconciledTransferSave
    else -> null
  }

private fun deleteDialog(saved: TransactionDetails?): TransactionDialog =
  when {
    saved?.reconciled == true -> ConfirmReconciledDelete
    saved?.hasReconciledTransfer == true -> ConfirmReconciledTransferDelete
    else -> ConfirmDelete
  }

// A transaction deleted elsewhere can only have its draft thrown away, from the toolbar
@Composable
private fun BoxScope.SaveRevertFabs(
  state: EditTransactionState.Loaded,
  canSave: Boolean,
  isVisible: Boolean,
  scrollState: ScrollState,
  onSave: () -> Unit,
  onRevert: () -> Unit,
) =
  Column(modifier = Modifier.align(BottomCenter)) {
    val mode = state.mode
    val hasChanges = mode.form.hasChanges
    val isGone = state.changedElsewhere == Deleted
    EditFabs(
      saveText = Strings.transactionSave,
      discardText = Strings.transactionRevert,
      onSave = onSave,
      onDiscard = onRevert,
      // A saved transaction only offers saving once it's been changed
      isSaveVisible = isVisible && (mode is Create || hasChanges && !isGone),
      isDiscardVisible = isVisible && mode is Edit && hasChanges && !isGone,
      canSave = canSave,
      isSaving = state.isWorking,
      isExpanded = scrollState.isScrollingUp(),
    )
    BottomSpacing()
  }

@Composable
private fun TopBarActions(
  state: EditTransactionState.Loaded,
  onDelete: () -> Unit,
  modifier: Modifier = Modifier,
) {
  if (state.mode !is Edit || state.changedElsewhere == Deleted) return
  Box(modifier = modifier) {
    var expanded by remember { mutableStateOf(false) }
    AktualIconButton(
      imageVector = AktualIcons.DotsHorizontalTriple,
      contentDescription = Strings.transactionMore,
      isEnabled = !state.isWorking,
      onClick = { expanded = true },
    )
    AktualDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
      AktualDropdownMenuItem(
        text = Strings.transactionDelete,
        leadingIcon = AktualIcons.Trash,
        onClick = {
          expanded = false
          onDelete()
        },
      )
    }
  }
}

@Composable
private fun LoadFailure(
  state: EditTransactionState.Failure,
  onAction: OnAction<EditTransactionAction>,
  modifier: Modifier = Modifier,
) =
  FailureScreen(
    modifier = modifier,
    title = Strings.transactionFailureTitle,
    reason =
      when (state) {
        NotFound -> Strings.transactionFailureNotFound
        is OtherFailure -> state.reason
      },
    action =
      FailureAction(
        text = { Strings.navBack },
        icon = AktualIcons.CheveronLeft,
        onClick = { onAction(NavigateBack) },
      ),
  )

// As upstream's mobile editor, which sends you off to add what a new transaction needs first
@Composable
private fun NothingToCreateWith(
  state: EditTransactionState.Empty,
  onAction: OnAction<EditTransactionAction>,
  modifier: Modifier = Modifier,
) =
  when (state) {
    NoAccounts ->
      FailureScreen(
        modifier = modifier,
        title = Strings.transactionCreateNoAccountsTitle,
        reason = Strings.transactionCreateNoAccounts,
        icon = null,
        action =
          FailureAction(
            text = { Strings.navBack },
            icon = AktualIcons.CheveronLeft,
            onClick = { onAction(NavigateBack) },
          ),
      )

    NoCategories ->
      FailureScreen(
        modifier = modifier,
        title = Strings.transactionCreateNoCategoriesTitle,
        reason = Strings.transactionCreateNoCategories,
        icon = null,
        action =
          FailureAction(
            text = { Strings.transactionCreateGoToBudget },
            icon = AktualIcons.CheveronRight,
            onClick = { onAction(GoToBudget) },
          ),
      )
  }

@Composable
internal inline fun DetailCard(content: @Composable ColumnScope.() -> Unit) =
  Column(
    modifier = Modifier.fillMaxWidth().background(colors.tableBackground, DetailCardShape),
    content = content,
  )

@Composable
internal fun BalanceAfter(balance: Amount, modifier: Modifier = Modifier) =
  CardRow(
    modifier = modifier.fillMaxWidth(),
    contentPadding = BalancePadding,
    verticalAlignment = CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Text(
      text = Strings.transactionBalanceAfter,
      fontSize = DateSize,
      color = colors.pageTextLight,
    )
    Text(
      text = balance.formattedText(),
      fontSize = ValueSize,
      fontWeight = SemiBold,
      color = balance.color(),
      style = tabularFigures(),
    )
  }

internal val CardInset = 16.dp
internal val CardSpacing = 12.dp
internal val DetailCardShape = RoundedCornerShape(10.dp)
internal val HeroAmountSize = textSizes.hero
internal val HeroAmountTracking = letterSpacings.tight
private val DateSize = textSizes.medium
internal val ValueSize = textSizes.mediumPlus
private val BalancePadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
internal val SplitHeaderPadding = PaddingValues(start = 4.dp, bottom = 8.dp)
internal val SplitHeaderSize = textSizes.smallPlus
internal val SplitHeaderTracking = letterSpacings.wider

@PortraitPreview
@Composable
private fun PreviewEditTransactionScaffold(
  @PreviewParameter(EditTransactionStateProvider::class) params: ColorParams<EditTransactionState>,
) =
  PreviewWithColorParams(params) {
    EditTransactionScaffold(state = this, error = null, onAction = {})
  }

private fun previewLoaded(
  transaction: Transaction,
  form: TransactionForm = previewForm(transaction),
) =
  EditTransactionState.Loaded(
    saved =
      TransactionDetails(
        transaction,
        cleared = form.draft.cleared,
        reconciled = form.draft.reconciled,
      ),
    mode = TransactionEditMode.Edit(form),
  )

private fun previewForm(
  transaction: Transaction,
  cleared: Boolean = true,
  reconciled: Boolean = false,
  hasChanges: Boolean = false,
) =
  TransactionForm(
    draft =
      TransactionFields(
        account = AccountId("account"),
        date = transaction.date,
        amount = transaction.amount,
        payee = PayeeId("payee"),
        category = CategoryId("category"),
        notes = transaction.notes,
        cleared = cleared,
        reconciled = reconciled,
      ),
    payeeName = transaction.payee,
    categoryName = transaction.category,
    accountName = transaction.account,
    isOffBudget = transaction.specialCategory == OffBudget,
    isTransfer = transaction.specialCategory == Transfer,
    hasChanges = hasChanges,
    options =
      TransactionOptions(
        payees = persistentListOf(),
        transfers = persistentListOf(),
        categoryGroups = persistentListOf(),
        accounts = persistentListOf(),
      ),
  )

private val PREVIEW_SPLIT_FORM =
  previewForm(TRANSACTION_SPLIT_UNBALANCED, hasChanges = true)
    .copy(
      parts =
        TRANSACTION_SPLIT_UNBALANCED.children
          .map { child ->
            SplitPartForm(
              part = SplitPart(id = child.id, amount = child.amount, notes = child.notes),
              payeeName = child.payee,
              categoryName = child.category,
            )
          }
          .toImmutableList(),
      splitRemaining = TRANSACTION_SPLIT_UNBALANCED.splitRemaining,
    )

private val PREVIEW_NEW =
  TransactionForm(
    draft =
      TransactionFields(
        account = null,
        date = TRANSACTION_1.date,
        amount = Zero,
        payee = null,
        category = null,
        notes = null,
        cleared = false,
        reconciled = false,
      ),
    payeeName = null,
    categoryName = null,
    accountName = null,
    isOffBudget = false,
    hasChanges = false,
    options =
      TransactionOptions(
        payees = persistentListOf(),
        transfers = persistentListOf(),
        categoryGroups = persistentListOf(),
        accounts = persistentListOf(),
      ),
  )

private class EditTransactionStateProvider :
  ColorParamsProvider<EditTransactionState>(
    Loading,
    NotFound,
    previewLoaded(TRANSACTION_1),
    previewLoaded(TRANSACTION_1, previewForm(TRANSACTION_1, hasChanges = true)),
    previewLoaded(TRANSACTION_1, previewForm(TRANSACTION_1, hasChanges = true))
      .copy(changedElsewhere = Conflicting),
    previewLoaded(TRANSACTION_1, previewForm(TRANSACTION_1, hasChanges = true))
      .copy(changedElsewhere = Deleted),
    previewLoaded(
      TRANSACTION_TRANSFER.copy(notes = null),
      previewForm(TRANSACTION_TRANSFER.copy(notes = null), reconciled = true),
    ),
    previewLoaded(TRANSACTION_OFF_BUDGET, previewForm(TRANSACTION_OFF_BUDGET, cleared = false)),
    previewLoaded(TRANSACTION_SPLIT_UNBALANCED, PREVIEW_SPLIT_FORM),
    EditTransactionState.Loaded(
      saved = null,
      mode = TransactionEditMode.Create(PREVIEW_NEW),
    ),
    NoAccounts,
    NoCategories,
  )
