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
import aktual.budget.transactions.ui.label
import aktual.budget.transactions.ui.payeeLabel
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
import aktual.core.icons.AktualIcons
import aktual.core.icons.LeftArrow2
import aktual.core.icons.RightArrow2
import aktual.core.icons.Split
import aktual.core.icons.Tag
import aktual.core.icons.material.AccountBalanceWallet
import aktual.core.icons.material.ArrowBack
import aktual.core.icons.material.ArrowRight
import aktual.core.icons.material.Clear
import aktual.core.icons.material.Delete
import aktual.core.icons.material.Edit
import aktual.core.icons.material.FormatListBulleted
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.MoreVert
import aktual.core.l10n.Strings
import aktual.core.nav.BackNavigator
import aktual.core.ui.AktualDropdownMenu
import aktual.core.ui.AktualDropdownMenuItem
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.BackHandler
import aktual.core.ui.BareIconButton
import aktual.core.ui.BottomSpacing
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.FailureAction
import aktual.core.ui.FailureScreen
import aktual.core.ui.NavBackIconButton
import aktual.core.ui.PageBackground
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.PrimaryTextButton
import aktual.core.ui.formatted
import aktual.core.ui.formattedString
import aktual.core.ui.formattedText
import aktual.core.ui.hazedTopBar
import aktual.core.ui.hazedTopBarContent
import aktual.core.ui.hazedTopBarContentPadding
import aktual.core.ui.redacted
import aktual.core.ui.rememberHazedTopBarState
import aktual.core.ui.stringLong
import aktual.core.ui.transparentTopAppBarColors
import androidx.compose.foundation.ScrollState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEachIndexed
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
        StartEditing -> viewModel.startEditing()
        StopEditing -> viewModel.stopEditing()
        SaveTransaction -> viewModel.save()
        DeleteTransaction -> viewModel.delete()
        UnlockReconciled -> viewModel.unlockReconciled()
        DismissError -> viewModel.dismissError()
        CommitAmount -> viewModel.commitAmount()
        GoToBudget -> toBudget()
        is SetAmount -> viewModel.setAmount(action.amount)
        is SetPayee -> viewModel.setPayee(action.id)
        is CreatePayee -> viewModel.createPayee(action.name)
        is SetCategory -> viewModel.setCategory(action.id)
        is SetAccount -> viewModel.setAccount(action.id)
        is SetDate -> viewModel.setDate(action.date)
        is SetNotes -> viewModel.setNotes(action.notes)
        is SetCleared -> viewModel.setCleared(action.cleared)
        is SplitAction -> viewModel.onSplitAction(action)
      }
    },
  )
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
  val loaded = state as? EditTransactionState.Loaded
  val form = (loaded?.mode as? TransactionEditMode.Editing)?.form
  val isEditing = form != null
  val isCreating = loaded?.mode is Create
  var dialog by remember { mutableStateOf<TransactionDialog?>(null) }
  // A new transaction starts with the keypad open
  val fieldState =
    remember(isEditing) {
      EditFieldState(if (isCreating) ActiveField(EditField.Amount) else null)
    }
  val isAmountValid = !fieldState.isTypingAmount || fieldState.amountText.isValidAmount()

  // Backing out of an edit drops back to viewing the transaction, or closes a new one, checking
  // first if that would lose any changes
  fun onBack() {
    when {
      loaded?.isWorking == true -> Unit
      form == null -> onAction(NavigateBack)
      form.hasChanges -> dialog = ConfirmDiscard
      else -> onAction(StopEditing)
    }
  }
  BackHandler(enabled = isEditing) { onBack() }

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
        onEdit = { onAction(StartEditing) },
        onSave = {
          if (form?.draft?.reconciled == true) {
            dialog = ConfirmReconciledSave
          } else {
            onAction(SaveTransaction)
          }
        },
        onDelete = {
          dialog = if (loaded?.saved?.reconciled == true) ConfirmReconciledDelete else ConfirmDelete
        },
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
          if (form == null) {
            state.saved?.let { saved ->
              TransactionContent(
                modifier = Modifier.hazedTopBarContent(hazeState, innerPadding),
                details = saved,
                scrollState = scrollState,
                contentPadding = hazedTopBarContentPadding(hazeState, innerPadding),
              )
            }
          } else {
            EditTransactionForm(
              modifier = Modifier.hazedTopBarContent(hazeState, innerPadding),
              form = form,
              fieldState = fieldState,
              onUnlock = { dialog = ConfirmReconciledUnlock },
              onAction = onAction,
              scrollState = scrollState,
              contentPadding = hazedTopBarContentPadding(hazeState, innerPadding),
            )
          }
      }

      if (form != null) {
        EditTransactionSheet(
          active = fieldState.active,
          form = form,
          onAction = onAction,
          onDismiss = { fieldState.close() },
        )
      }

      EditTransactionDialogs(dialog = dialog, onAction = onAction, onShow = { dialog = it })

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
  onEdit: () -> Unit,
  onSave: () -> Unit,
  onDelete: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val loaded = state as? EditTransactionState.Loaded
  val isEditing = loaded?.mode is Editing
  val isCreating = loaded?.mode is Create || state is Empty
  TopAppBar(
    modifier = modifier,
    colors = colors.transparentTopAppBarColors(),
    navigationIcon = {
      if (isEditing) {
        IconButton(onClick = onBack, enabled = !loaded.isWorking) {
          Icon(
            imageVector = MaterialIcons.Clear,
            contentDescription = Strings.transactionStopEditing,
          )
        }
      } else {
        NavBackIconButton(onClick = onBack)
      }
    },
    title = {
      when {
        isCreating -> Text(text = Strings.transactionCreateTitle)
        isEditing -> Text(text = Strings.transactionEditTitle)
      }
    },
    actions = {
      if (loaded != null) {
        TopBarActions(
          state = loaded,
          canSave = canSave,
          onEdit = onEdit,
          onSave = onSave,
          onDelete = onDelete,
        )
      }
    },
  )
}

@Composable
private fun TopBarActions(
  state: EditTransactionState.Loaded,
  canSave: Boolean,
  onEdit: () -> Unit,
  onSave: () -> Unit,
  onDelete: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(modifier = modifier, verticalAlignment = CenterVertically) {
    if (state.mode is Editing) {
      PrimaryTextButton(
        modifier = Modifier.padding(end = SaveInset),
        text = Strings.transactionSave,
        isEnabled = canSave,
        shape = SaveShape,
        onClick = onSave,
      )
    } else if (state.canEdit) {
      BareIconButton(
        imageVector = MaterialIcons.Edit,
        contentDescription = Strings.transactionEdit,
        enabled = !state.isWorking,
        onClick = onEdit,
      )

      Box {
        var expanded by remember { mutableStateOf(false) }
        BareIconButton(
          imageVector = MaterialIcons.MoreVert,
          contentDescription = Strings.transactionMore,
          enabled = !state.isWorking,
          onClick = { expanded = true },
        )
        AktualDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
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

// Stands in for the hero and the details card while the transaction loads
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
      Box(modifier = Modifier.size(AvatarSize).background(colors.tableText, CircleShape))
      Box(modifier = bar.size(width = ShimmerPayeeWidth, height = ShimmerTextHeight))
      Box(modifier = bar.size(width = ShimmerAmountWidth, height = ShimmerAmountHeight))
      Box(modifier = bar.size(width = ShimmerDateWidth, height = ShimmerTextHeight))
    }

    DetailCard {
      repeat(SHIMMER_ROWS) { index ->
        if (index > 0) CardDivider()
        Row(
          modifier = Modifier.fillMaxWidth().padding(RowPadding),
          verticalAlignment = CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(RowIconGap),
        ) {
          Box(modifier = bar.size(RowIconSize))
          Box(modifier = bar.weight(1f).height(ShimmerRowHeight))
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

// Option A of the transaction editor designs: the payee and amount up top, the rest in cards
// under them. A split lists its parts first, in place of the category
@Composable
private fun TransactionContent(
  details: TransactionDetails,
  scrollState: ScrollState,
  contentPadding: PaddingValues,
  modifier: Modifier = Modifier,
) {
  val transaction = details.transaction
  val isSplit = transaction.children.isNotEmpty()
  Column(
    modifier =
      modifier
        .fillMaxSize()
        .verticalScroll(scrollState)
        .padding(contentPadding)
        .padding(horizontal = CardInset),
    verticalArrangement = Arrangement.spacedBy(CardSpacing),
  ) {
    Hero(details)

    if (isSplit) {
      SplitParts(transaction)
      DetailCard {
        AccountRow(transaction)
        CardDivider()
        NotesRow(transaction)
      }
    } else {
      DetailCard {
        DetailRow(AktualIcons.Tag, Strings.transactionCategory) { CategoryValue(transaction) }
        CardDivider()
        AccountRow(transaction)
        CardDivider()
        NotesRow(transaction)
      }

      transaction.balance?.let { BalanceAfter(it) }
    }

    BottomSpacing()
  }
}

@Composable
private fun Hero(details: TransactionDetails, modifier: Modifier = Modifier) {
  val transaction = details.transaction
  val payee = transaction.payeeText()
  Column(
    modifier = modifier.fillMaxWidth().padding(HeroPadding),
    horizontalAlignment = CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(HeroSpacing),
  ) {
    Avatar(initial = transaction.payee?.firstOrNull { it.isLetterOrDigit() })

    Row(
      verticalAlignment = CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(IconGap),
    ) {
      transaction.transferIcon()?.let { icon ->
        Icon(
          modifier = Modifier.size(TransferIconSize),
          imageVector = icon,
          contentDescription = null,
          tint = colors.pageText,
        )
      }
      Text(
        text = payee ?: Strings.transactionsSplitNoPayee,
        fontSize = PayeeSize,
        fontWeight = SemiBold,
        fontStyle = if (payee == null) Italic else null,
        color = if (payee == null) colors.pageTextLight else colors.pageText,
        textAlign = Center,
      )
    }

    Text(
      text = transaction.amount.formattedText(includeSign = true),
      fontSize = HeroAmountSize,
      fontWeight = Bold,
      letterSpacing = HeroAmountTracking,
      color = transaction.amount.heroColor(),
      style = tabularFigures(),
      maxLines = 1,
    )

    Row(
      verticalAlignment = CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(PillGap),
    ) {
      Text(
        text = "${transaction.date.dayOfWeek.stringLong()} ${transaction.date.formatted()}",
        fontSize = DateSize,
        color = colors.pageTextLight,
      )
      StatusPill(details)
    }
  }
}

@Composable
private fun Avatar(initial: Char?, modifier: Modifier = Modifier) =
  Box(
    modifier = modifier.size(AvatarSize).background(colors.mobileHeaderBackground, CircleShape),
    contentAlignment = Center,
  ) {
    if (initial != null) {
      Text(
        text = initial.uppercase(),
        fontSize = AvatarTextSize,
        fontWeight = Bold,
        color = colors.mobileHeaderText,
      )
    } else {
      Icon(
        modifier = Modifier.size(AvatarIconSize),
        imageVector = AktualIcons.Split,
        contentDescription = null,
        tint = colors.mobileHeaderText,
      )
    }
  }

@Composable
private fun StatusPill(details: TransactionDetails, modifier: Modifier = Modifier) {
  val isCleared = details.cleared || details.reconciled
  Text(
    modifier =
      modifier
        .background(
          color = if (isCleared) colors.noticeBackgroundLight else colors.pillBackground,
          shape = PillShape,
        )
        .padding(PillPadding),
    text =
      when {
        details.reconciled -> Strings.transactionReconciled
        details.cleared -> Strings.transactionCleared
        else -> Strings.transactionUncleared
      },
    fontSize = PillTextSize,
    fontWeight = SemiBold,
    color = if (isCleared) colors.noticeText else colors.pillText,
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
private fun DetailRow(icon: ImageVector, label: String, value: @Composable () -> Unit) =
  Row(
    modifier = Modifier.fillMaxWidth().padding(RowPadding),
    verticalAlignment = CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(RowIconGap),
  ) {
    Icon(
      modifier = Modifier.size(RowIconSize),
      imageVector = icon,
      contentDescription = null,
      tint = colors.pageTextSubdued,
    )
    Column(verticalArrangement = Arrangement.spacedBy(LabelGap)) {
      Text(text = label, fontSize = LabelSize, color = colors.pageTextSubdued)
      value()
    }
  }

@Composable
private fun AccountRow(transaction: Transaction) =
  DetailRow(MaterialIcons.AccountBalanceWallet, Strings.transactionAccount) {
    Value(transaction.account)
  }

@Composable
private fun NotesRow(transaction: Transaction) =
  DetailRow(MaterialIcons.FormatListBulleted, Strings.transactionNotes) {
    Value(transaction.notes?.takeIf { it.isNotBlank() })
  }

// A missing value reads as "None", as upstream's mobile editor shows it
@Composable
private fun Value(text: String?, color: Color = colors.pageText) =
  Text(
    text = text ?: Strings.transactionNone,
    fontSize = ValueSize,
    color = if (text == null) colors.pageTextSubdued else color,
    fontStyle = if (text == null) Italic else null,
  )

// What the list shows in place of the category, so a transfer and an off budget transaction read
// the same here
@Composable
private fun CategoryValue(transaction: Transaction) =
  Value(text = transaction.categoryText(), color = transaction.categoryColor())

@Composable
private fun Transaction.categoryText(): String? =
  when {
    specialCategory != null -> specialCategory?.label()
    needsCategory -> Strings.transactionsNeedsCategory
    else -> category
  }

@Composable
@ReadOnlyComposable
private fun Transaction.categoryColor(): Color =
  if (specialCategory == null && needsCategory) colors.warningText else colors.pageText

@Composable
private fun BalanceAfter(balance: Amount, modifier: Modifier = Modifier) =
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

@Composable
private fun SplitParts(parent: Transaction, modifier: Modifier = Modifier) =
  Column(modifier = modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.padding(SplitHeaderPadding),
      verticalAlignment = CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(SplitHeaderGap),
    ) {
      Icon(
        modifier = Modifier.size(SplitIconSize),
        imageVector = AktualIcons.Split,
        contentDescription = null,
        tint = colors.pageTextLight,
      )
      Text(
        text = Strings.transactionSplitInto(parent.totalChildren).uppercase(),
        fontSize = SplitHeaderSize,
        fontWeight = SemiBold,
        letterSpacing = SplitHeaderTracking,
        color = colors.pageTextLight,
      )
    }

    DetailCard {
      parent.children.fastForEachIndexed { index, child ->
        if (index > 0) CardDivider()
        SplitPartRow(child, parent)
      }

      val remaining = parent.splitRemaining
      if (remaining != null) {
        CardDivider()
        Text(
          modifier = Modifier.fillMaxWidth().padding(PartPadding),
          text = Strings.transactionsSplitRemaining(remaining.formattedString()).redacted(),
          fontSize = ValueSize,
          fontWeight = SemiBold,
          color = colors.warningText,
          textAlign = End,
        )
      }
    }
  }

@Composable
private fun SplitPartRow(child: Transaction, parent: Transaction) =
  Row(
    modifier = Modifier.fillMaxWidth().padding(PartPadding),
    verticalAlignment = CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(PartGap),
  ) {
    Box(
      modifier =
        Modifier.size(width = PartBarWidth, height = PartBarHeight)
          .background(colors.buttonPrimaryBackground, PartBarShape),
    )

    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(LabelGap)) {
      CategoryValue(child)
      val subtitle = child.partSubtitle(parent)
      if (subtitle != null) {
        Text(text = subtitle, fontSize = LabelSize, color = colors.pageTextLight)
      }
    }

    Text(
      text = child.amount.formattedText(includeSign = true),
      fontSize = ValueSize,
      color = child.amount.heroColor(),
      style = tabularFigures(),
    )
  }

// A part's notes, then its payee when it isn't the one in the hero
@Composable
private fun Transaction.partSubtitle(parent: Transaction): AnnotatedString? {
  val payee = payeeLabel().takeIf { payee != parent.payee || transfer != parent.transfer }
  val notes = notes?.takeIf { it.isNotBlank() }
  val text = listOfNotNull(notes, payee).joinToString(" · ")
  return if (text.isEmpty()) null else AnnotatedString(text)
}

@Composable
private fun Transaction.payeeText(): String? =
  if (split == Parent && payee == null) null else payeeLabel()

private fun Transaction.transferIcon(): ImageVector? =
  when (transfer) {
    To -> AktualIcons.RightArrow2
    From -> AktualIcons.LeftArrow2
    null -> null
  }

// Outgoing money is red here, where the list leaves it plain
@ReadOnlyComposable
@Composable
private fun Amount.heroColor(): Color = if (this < Zero) colors.numberNegative else color()

internal val CardInset = 16.dp
internal val CardSpacing = 12.dp
internal val DetailCardShape = RoundedCornerShape(10.dp)
private val HeroPadding = PaddingValues(start = 8.dp, top = 12.dp, end = 8.dp, bottom = 10.dp)
private val HeroSpacing = 8.dp
private val AvatarSize = 56.dp
private val AvatarIconSize = 24.dp
private val AvatarTextSize = 22.sp
private val PayeeSize = 18.sp
internal val HeroAmountSize = 44.sp
internal val HeroAmountTracking = (-1).sp
private val DateSize = 14.sp
private val SaveShape = RoundedCornerShape(percent = 50)
private val SaveInset = 8.dp
private val PillGap = 8.dp
private val PillShape = RoundedCornerShape(12.dp)
private val PillPadding = PaddingValues(horizontal = 10.dp, vertical = 3.dp)
private val PillTextSize = 12.sp
private val TransferIconSize = 16.dp
private val IconGap = 6.dp
private val RowPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
private val RowIconSize = 20.dp
private val RowIconGap = 14.dp
private val LabelGap = 2.dp
private val LabelSize = 12.sp
internal val ValueSize = 15.sp
private val BalancePadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
internal val SplitHeaderPadding = PaddingValues(start = 4.dp, bottom = 8.dp)
private val SplitHeaderGap = 8.dp
private val SplitIconSize = 16.dp
internal val SplitHeaderSize = 13.sp
internal val SplitHeaderTracking = 1.sp
private val PartPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
private val PartGap = 12.dp
private val PartBarWidth = 4.dp
private val PartBarHeight = 32.dp
private val PartBarShape = RoundedCornerShape(2.dp)
private val ShimmerBarShape = RoundedCornerShape(4.dp)
private val ShimmerPayeeWidth = 140.dp
private val ShimmerAmountWidth = 180.dp
private val ShimmerAmountHeight = 44.dp
private val ShimmerDateWidth = 200.dp
private val ShimmerTextHeight = 18.dp
private val ShimmerRowHeight = 32.dp
private const val SHIMMER_ROWS = 3

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
  cleared: Boolean = true,
  reconciled: Boolean = false,
  mode: TransactionEditMode = View,
) =
  EditTransactionState.Loaded(
    saved = TransactionDetails(transaction, cleared = cleared, reconciled = reconciled),
    mode = mode,
    canEdit = true,
  )

private fun previewForm(transaction: Transaction, reconciled: Boolean = false) =
  TransactionForm(
    draft =
      TransactionFields(
        account = AccountId("account"),
        date = transaction.date,
        amount = transaction.amount,
        payee = PayeeId("payee"),
        category = CategoryId("category"),
        notes = transaction.notes,
        cleared = true,
        reconciled = reconciled,
      ),
    payeeName = transaction.payee,
    categoryName = transaction.category,
    accountName = transaction.account,
    isOffBudget = transaction.specialCategory == OffBudget,
    hasChanges = true,
    options =
      TransactionOptions(
        payees = persistentListOf(),
        categoryGroups = persistentListOf(),
        accounts = persistentListOf(),
      ),
  )

private val PREVIEW_SPLIT_FORM =
  previewForm(TRANSACTION_SPLIT_UNBALANCED)
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
        categoryGroups = persistentListOf(),
        accounts = persistentListOf(),
      ),
  )

private class EditTransactionStateProvider :
  ColoredParameterProvider<EditTransactionState>(
    Loading,
    EditTransactionState.Failure.NotFound,
    previewLoaded(TRANSACTION_1),
    previewLoaded(TRANSACTION_TRANSFER.copy(notes = null), reconciled = true),
    previewLoaded(TRANSACTION_OFF_BUDGET, cleared = false),
    previewLoaded(TRANSACTION_SPLIT_UNBALANCED),
    previewLoaded(TRANSACTION_1, mode = TransactionEditMode.Edit(previewForm(TRANSACTION_1))),
    previewLoaded(
      TRANSACTION_OFF_BUDGET,
      reconciled = true,
      mode = TransactionEditMode.Edit(previewForm(TRANSACTION_OFF_BUDGET, reconciled = true)),
    ),
    previewLoaded(
      TRANSACTION_SPLIT_UNBALANCED,
      mode = TransactionEditMode.Edit(PREVIEW_SPLIT_FORM),
    ),
    EditTransactionState.Loaded(
      saved = null,
      mode = TransactionEditMode.Create(PREVIEW_NEW),
      canEdit = true,
    ),
    EditTransactionState.Empty.NoAccounts,
    EditTransactionState.Empty.NoCategories,
  )
