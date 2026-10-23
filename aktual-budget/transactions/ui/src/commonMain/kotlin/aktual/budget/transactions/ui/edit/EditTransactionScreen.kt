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
import aktual.budget.transactions.vm.edit.EditTransactionState
import aktual.budget.transactions.vm.edit.EditTransactionTarget
import aktual.budget.transactions.vm.edit.EditTransactionViewModel
import aktual.budget.transactions.vm.edit.SplitPartForm
import aktual.budget.transactions.vm.edit.TransactionDetails
import aktual.budget.transactions.vm.edit.TransactionEditMode
import aktual.budget.transactions.vm.edit.TransactionForm
import aktual.budget.transactions.vm.edit.TransactionOptions
import aktual.core.icons.material.ArrowBack
import aktual.core.icons.material.ArrowRight
import aktual.core.icons.material.Delete
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.MoreVert
import aktual.core.icons.material.Undo
import aktual.core.l10n.Strings
import aktual.core.nav.BackNavigator
import aktual.core.ui.AktualDropdownMenu
import aktual.core.ui.AktualDropdownMenuItem
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.BackHandler
import aktual.core.ui.BareIconButton
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.FailureAction
import aktual.core.ui.FailureScreen
import aktual.core.ui.NavBackIconButton
import aktual.core.ui.PageBackground
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.PrimaryTextButton
import aktual.core.ui.formattedText
import aktual.core.ui.hazedTopBar
import aktual.core.ui.hazedTopBarContent
import aktual.core.ui.hazedTopBarContentPadding
import aktual.core.ui.rememberHazedTopBarState
import aktual.core.ui.transparentTopAppBarColors
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

@Composable
internal fun EditTransactionScreen(
  target: EditTransactionTarget,
  back: BackNavigator,
  toBudget: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: EditTransactionViewModel = editTransactionViewModel(target),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  val error by viewModel.error.collectAsStateWithLifecycle()

  LaunchedEffect(viewModel) {
    viewModel.events.collect { event ->
      when (event) {
        Deleted,
        Created,
        Discarded -> back()
      }
    }
  }

  EditTransactionScaffold(
    modifier = modifier,
    state = state,
    error = error,
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
        is FieldAction -> viewModel.onFieldAction(action)
        is SplitAction -> viewModel.onSplitAction(action)
      }
    },
  )
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

@Composable
private fun editTransactionViewModel(target: EditTransactionTarget) =
  assistedMetroViewModel<EditTransactionViewModel, EditTransactionViewModel.Factory>(
    key = target.toString(),
  ) {
    create(target)
  }

@Composable
private fun EditTransactionScaffold(
  state: EditTransactionState,
  error: EditTransactionError?,
  onAction: EditTransactionActionHandler,
  modifier: Modifier = Modifier,
) {
  val loaded = state as? Loaded
  val form = loaded?.mode?.form
  val isLoaded = form != null
  val isCreating = loaded?.mode is Create
  var dialog by remember { mutableStateOf<TransactionDialog?>(null) }
  // A new transaction starts with the keypad open
  val fieldState =
    remember(isLoaded) { EditFieldState(if (isCreating) ActiveField(EditField.Amount) else null) }
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

  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      EditTransactionTopBar(
        modifier = Modifier.hazedTopBar(hazeState, scrollOffset = { scrollState.value.toFloat() }),
        state = state,
        canSave = loaded?.canSave == true && isAmountValid,
        onBack = { onBack() },
        onRevert = { onAction(RevertChanges) },
        onSave = {
          val confirm =
            if (loaded?.changedElsewhere == Conflicting) {
              ConfirmOverwrite
            } else {
              saveDialog(form, loaded?.saved)
            }
          if (confirm == null) onAction(SaveTransaction) else dialog = confirm
        },
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
  canSave: Boolean,
  onBack: () -> Unit,
  onRevert: () -> Unit,
  onSave: () -> Unit,
  onDelete: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val loaded = state as? Loaded
  val isCreating = loaded?.mode is Create || state is Empty
  TopAppBar(
    modifier = modifier,
    colors = colors.transparentTopAppBarColors(),
    navigationIcon = { NavBackIconButton(onClick = onBack) },
    title = { if (isCreating) Text(text = Strings.transactionCreateTitle) },
    actions = {
      if (loaded != null) {
        TopBarActions(
          state = loaded,
          canSave = canSave,
          onRevert = onRevert,
          onSave = onSave,
          onDelete = onDelete,
        )
      }
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

@Composable
private fun TopBarActions(
  state: EditTransactionState.Loaded,
  canSave: Boolean,
  onRevert: () -> Unit,
  onSave: () -> Unit,
  onDelete: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val mode = state.mode
  val hasChanges = mode.form.hasChanges
  // A transaction deleted elsewhere can only have its draft thrown away
  val isGone = state.changedElsewhere == Deleted
  Row(modifier = modifier, verticalAlignment = CenterVertically) {
    // A saved transaction only offers saving once it's been changed
    AnimatedVisibility(
      visible = mode is Create || hasChanges && !isGone,
      enter = fadeIn(),
      exit = fadeOut(),
    ) {
      PrimaryTextButton(
        modifier = Modifier.padding(end = SaveInset),
        text = Strings.transactionSave,
        isEnabled = canSave,
        shape = SaveShape,
        onClick = onSave,
      )
    }

    if (mode is Edit && !isGone) {
      Box {
        var expanded by remember { mutableStateOf(false) }
        BareIconButton(
          imageVector = MaterialIcons.MoreVert,
          contentDescription = Strings.transactionMore,
          enabled = !state.isWorking,
          onClick = { expanded = true },
        )
        AktualDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
          if (hasChanges) {
            AktualDropdownMenuItem(
              text = Strings.transactionRevert,
              leadingIcon = MaterialIcons.Undo,
              onClick = {
                expanded = false
                onRevert()
              },
            )
          }
          AktualDropdownMenuItem(
            text = Strings.transactionDelete,
            leadingIcon = MaterialIcons.Delete,
            onClick = {
              expanded = false
              onDelete()
            },
          )
        }
      }
    }
  }
}

// Stands in for the amount and the fields card while the transaction loads
@Composable
private fun ShimmerTransaction(modifier: Modifier = Modifier) {
  val bar = Modifier.background(colors.tableText, ShimmerBarShape)
  Column(
    modifier =
      modifier.fillMaxSize().padding(horizontal = CardInset).shimmer(rememberShimmer(Window)),
    verticalArrangement = Arrangement.spacedBy(CardSpacing),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(HeroPadding),
      horizontalAlignment = CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(HeroSpacing),
    ) {
      Box(modifier = bar.size(width = ShimmerToggleWidth, height = ShimmerToggleHeight))
      Box(modifier = bar.size(width = ShimmerAmountWidth, height = ShimmerAmountHeight))
    }

    DetailCard {
      repeat(SHIMMER_ROWS) { index ->
        if (index > 0) CardDivider()
        Row(
          modifier = Modifier.fillMaxWidth().padding(RowPadding),
          verticalAlignment = CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(RowGap),
        ) {
          Box(modifier = bar.size(width = ShimmerLabelWidth, height = ShimmerTextHeight))
          Box(modifier = bar.weight(1f).height(ShimmerTextHeight))
        }
      }
    }
  }
}

@Composable
private fun LoadFailure(
  state: EditTransactionState.Failure,
  onAction: EditTransactionActionHandler,
  modifier: Modifier = Modifier,
) =
  FailureScreen(
    modifier = modifier,
    title = Strings.transactionFailureTitle,
    reason =
      when (state) {
        NotFound -> Strings.transactionFailureNotFound
        is Other -> state.reason
      },
    action =
      FailureAction(
        text = { Strings.navBack },
        icon = MaterialIcons.ArrowBack,
        onClick = { onAction(NavigateBack) },
      ),
  )

// As upstream's mobile editor, which sends you off to add what a new transaction needs first
@Composable
private fun NothingToCreateWith(
  state: EditTransactionState.Empty,
  onAction: EditTransactionActionHandler,
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
            icon = MaterialIcons.ArrowBack,
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
            icon = MaterialIcons.ArrowRight,
            onClick = { onAction(GoToBudget) },
          ),
      )
  }

@Composable
internal inline fun DetailCard(content: @Composable ColumnScope.() -> Unit) =
  Column(
    modifier = Modifier.fillMaxWidth().background(colors.cardBackground, DetailCardShape),
    content = content,
  )

@Composable internal fun CardDivider() = HorizontalDivider(color = colors.tableBorder)

@Composable
internal fun BalanceAfter(balance: Amount, modifier: Modifier = Modifier) =
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .background(colors.cardBackground, DetailCardShape)
        .padding(BalancePadding),
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
private val HeroPadding = PaddingValues(start = 8.dp, top = 8.dp, end = 8.dp, bottom = 4.dp)
private val HeroSpacing = 10.dp
internal val HeroAmountSize = 44.sp
internal val HeroAmountTracking = (-1).sp
private val DateSize = 14.sp
private val SaveShape = RoundedCornerShape(percent = 50)
private val SaveInset = 8.dp
private val RowPadding = PaddingValues(horizontal = 16.dp, vertical = 15.dp)
private val RowGap = 12.dp
internal val ValueSize = 15.sp
private val BalancePadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
internal val SplitHeaderPadding = PaddingValues(start = 4.dp, bottom = 8.dp)
internal val SplitHeaderSize = 13.sp
internal val SplitHeaderTracking = 1.sp
private val ShimmerBarShape = RoundedCornerShape(4.dp)
private val ShimmerToggleWidth = 200.dp
private val ShimmerToggleHeight = 30.dp
private val ShimmerAmountWidth = 180.dp
private val ShimmerAmountHeight = 44.dp
private val ShimmerLabelWidth = 72.dp
private val ShimmerTextHeight = 18.dp
private const val SHIMMER_ROWS = 6

@PortraitPreview
@Composable
private fun PreviewEditTransactionScaffold(
  @PreviewParameter(EditTransactionStateProvider::class)
  params: ColoredParams<EditTransactionState>,
) =
  PreviewWithColoredParams(params) {
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
  ColoredParameterProvider<EditTransactionState>(
    Loading,
    EditTransactionState.Failure.NotFound,
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
    EditTransactionState.Empty.NoAccounts,
    EditTransactionState.Empty.NoCategories,
  )
