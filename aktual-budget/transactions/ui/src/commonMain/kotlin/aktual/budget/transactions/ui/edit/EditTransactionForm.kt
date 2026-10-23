package aktual.budget.transactions.ui.edit

import aktual.budget.model.Amount
import aktual.budget.model.evaluateAmountInput
import aktual.budget.model.toInputText
import aktual.budget.model.toSignedInputText
import aktual.budget.tags.domain.TagLabel
import aktual.budget.transactions.domain.TransactionFields
import aktual.budget.transactions.ui.tabularFigures
import aktual.budget.transactions.vm.edit.ChangedElsewhere
import aktual.budget.transactions.vm.edit.TransactionForm
import aktual.core.icons.AktualIcons
import aktual.core.icons.Split
import aktual.core.icons.material.ExpandMore
import aktual.core.icons.material.MaterialIcons
import aktual.core.l10n.Strings
import aktual.core.ui.AktualSlidingToggleButton
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AmountKeypad
import aktual.core.ui.BareTextButton
import aktual.core.ui.BottomSpacing
import aktual.core.ui.RootOverlayContent
import aktual.core.ui.WarningBanner
import aktual.core.ui.formatted
import aktual.core.ui.formattedText
import aktual.core.ui.isInPreview
import aktual.core.ui.stringLong
import aktual.core.ui.switch
import aktual.core.ui.verticalScrollWithBar
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

internal fun TextFieldState.isValidAmount(): Boolean = evaluateAmountInput(text.toString()) != null

@Composable
private fun ChangedElsewhereBanner(change: ChangedElsewhere) =
  WarningBanner(
    text =
      when (change) {
        Conflicting -> Strings.transactionChangedElsewhere
        Deleted -> Strings.transactionDeletedElsewhere
      },
  )

// The amount up top, typed with the docked keypad, then a card of rows that each open a picker. A
// saved transaction opens straight into this, so anything on it can be changed in place. A split
// lists its parts under them, with amounts of their own
@Composable
internal fun EditTransactionForm(
  form: TransactionForm,
  fieldState: EditFieldState,
  onUnlock: () -> Unit,
  onAction: EditTransactionActionHandler,
  scrollState: ScrollState,
  contentPadding: PaddingValues,
  modifier: Modifier = Modifier,
  // The account's balance after the saved transaction, if it's been saved
  balance: Amount? = null,
  changedElsewhere: ChangedElsewhere? = null,
) {
  val draft = form.draft
  val amountText = fieldState.amountText
  val isTyping = fieldState.isTypingAmount
  val isTypingTotal = isTyping && fieldState.active?.part == null

  // Typed amounts are unsigned, and pick up their sign from the expense/income toggle. Zero has no
  // sign of its own, so the toggle is remembered for it
  var zeroIsIncome by remember { mutableStateOf(false) }
  val isIncome = if (draft.amount == Zero) zeroIsIncome else draft.amount > Zero

  TypedAmountEffect(fieldState = fieldState, isIncome = isIncome, onAction = onAction)
  RetypeAmountEffect(form = form, fieldState = fieldState, isIncome = isIncome)
  if (isTypingTotal) {
    DisposableEffect(Unit) { onDispose { onAction(CommitAmount) } }
  }

  val density = LocalDensity.current
  var keypadHeight by remember { mutableStateOf(0.dp) }
  val keypad: @Composable (Modifier) -> Unit = { keypadModifier ->
    DockedKeypad(
      modifier = keypadModifier,
      amountText = amountText,
      onDone = { fieldState.close() },
    )
  }
  val typeTotal = { fieldState.typeAmount(draft.amount.toInputText()) }

  Column(modifier = modifier.fillMaxSize()) {
    Column(
      modifier =
        Modifier.weight(1f)
          .verticalScrollWithBar(scrollState)
          .padding(contentPadding)
          .padding(horizontal = CardInset),
      verticalArrangement = Arrangement.spacedBy(CardSpacing),
    ) {
      changedElsewhere?.let { ChangedElsewhereBanner(it) }

      if (form.isSplit) {
        SplitTotal(
          amount = draft.amount,
          typed = amountText.text.toString(),
          isTyping = isTypingTotal,
          isIncome = isIncome,
          isValid = amountText.isValidAmount(),
          onClick = typeTotal,
        )

        SplitFieldsCard(form = form, onEdit = { fieldState.edit(it) })

        SplitParts(form = form, fieldState = fieldState, isIncome = isIncome, onAction = onAction)

        DetailCard {
          NotesRow(
            notes = draft.notes,
            tags = form.options.tags,
            onClick = { fieldState.edit(Notes) },
          )
          CardDivider()
          StatusRow(draft = draft, onUnlock = onUnlock, onAction = onAction)
        }
      } else {
        SignedAmount(
          amount = draft.amount,
          typed = amountText.text.toString(),
          isTyping = isTypingTotal,
          isIncome = isIncome,
          isValid = amountText.isValidAmount(),
          onClick = typeTotal,
          onSign = { income ->
            zeroIsIncome = income
            onAction(SetAmount(draft.amount.signed(income)))
          },
        )

        FieldsCard(
          form = form,
          onEdit = { fieldState.edit(it) },
          onUnlock = onUnlock,
          onAction = onAction,
        )

        // As upstream's mobile editor, which only splits a transaction that has an amount
        if (draft.amount != Zero) {
          BareTextButton(
            modifier = Modifier.align(CenterHorizontally),
            text = Strings.transactionsSplit,
            prefix = { Icon(imageVector = AktualIcons.Split, contentDescription = null) },
            onClick = { onAction(SplitTransaction) },
          )
        }

        balance?.let { BalanceAfter(it) }
      }

      // Leaves room to scroll the fields clear of the keypad
      if (isTyping) Spacer(Modifier.height(keypadHeight)) else BottomSpacing()
    }

    if (isInPreview()) {
      if (isTyping) keypad(Modifier)
    } else {
      RootOverlayContent {
        AnimatedVisibility(
          visible = isTyping,
          enter = slideInVertically { it },
          exit = slideOutVertically { it },
        ) {
          keypad(Modifier.onSizeChanged { keypadHeight = with(density) { it.height.toDp() } })
        }
      }
    }
  }
}

// Sends each amount typed to whichever amount is being edited, the transaction's or a part's
@Composable
private fun TypedAmountEffect(
  fieldState: EditFieldState,
  isIncome: Boolean,
  onAction: EditTransactionActionHandler,
) {
  val latestIsIncome by rememberUpdatedState(isIncome)
  val latestOnAction by rememberUpdatedState(onAction)
  LaunchedEffect(fieldState) {
    snapshotFlow { fieldState.active to fieldState.amountText.text.toString() }
      .collect { (active, text) ->
        if (active?.field != EditField.Amount) return@collect
        val amount = evaluateAmountInput(text) ?: return@collect
        val part = active.part
        if (part == null) {
          latestOnAction(SetAmount(amount.signed(latestIsIncome)))
        } else {
          latestOnAction(SetPartAmount(part, amount.negatedIf(!latestIsIncome)))
        }
      }
  }
}

// When the amount being typed changes in the draft some other way, e.g. by a sync, the text
// follows it, so the next key doesn't write the old amount back. Text that had already moved on
// from the old amount is being typed, so it's left alone
@Composable
private fun RetypeAmountEffect(
  form: TransactionForm,
  fieldState: EditFieldState,
  isIncome: Boolean,
) {
  val active = fieldState.active?.takeIf { it.field == EditField.Amount } ?: return
  val part = active.part
  val amount =
    if (part == null) form.draft.amount else form.parts.find { it.part.id == part }?.part?.amount
  var previous by remember(active) { mutableStateOf(amount) }
  SideEffect(active, amount) {
    val before = previous
    previous = amount
    if (amount == null || before == null || amount == before) return@SideEffect
    val typed = evaluateAmountInput(fieldState.amountText.text.toString()) ?: return@SideEffect
    if (part == null) {
      // The total is typed unsigned
      if (typed == before.abs() && typed != amount.abs()) {
        fieldState.typeAmount(amount.toInputText())
      }
    } else if (typed.negatedIf(!isIncome) == before) {
      fieldState.typeAmount(amount.negatedIf(!isIncome).toSignedInputText(), part)
    }
  }
}

// The expense/income toggle over the amount
@Composable
private fun SignedAmount(
  amount: Amount,
  typed: String,
  isTyping: Boolean,
  isIncome: Boolean,
  isValid: Boolean,
  onClick: () -> Unit,
  onSign: (Boolean) -> Unit,
) =
  Column(
    modifier = Modifier.fillMaxWidth().padding(AmountPadding),
    horizontalAlignment = CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(AmountSpacing),
  ) {
    AktualSlidingToggleButton(
      modifier = Modifier.width(ToggleWidth),
      selected = isIncome,
      options = persistentListOf(false, true),
      onSelect = onSign,
      string = { income -> if (income) Strings.transactionIncome else Strings.transactionExpense },
      fontSize = ToggleTextSize,
      itemPadding = TogglePadding,
    )

    HeroAmount(
      amount = amount,
      typed = typed,
      isTyping = isTyping,
      isIncome = isIncome,
      isValid = isValid,
      onClick = onClick,
    )
  }

// Covers the bottom status bar, and keeps clear of the system navigation bar
@Composable
private fun DockedKeypad(
  amountText: TextFieldState,
  onDone: () -> Unit,
  modifier: Modifier = Modifier,
) =
  Column(
    modifier =
      modifier
        .fillMaxWidth()
        .background(colors.cardBackground)
        // an empty pointer input stops clicks reaching the content underneath
        .pointerInput(Unit) {},
  ) {
    AmountKeypad(
      modifier = Modifier.fillMaxWidth().padding(KeypadPadding),
      state = amountText,
      canFinish = amountText.isValidAmount(),
      onDone = onDone,
    )
    BottomSpacing(height = 0.dp)
  }

@Composable
private fun FieldsCard(
  form: TransactionForm,
  onEdit: (EditField) -> Unit,
  onUnlock: () -> Unit,
  onAction: EditTransactionActionHandler,
) {
  val draft = form.draft
  DetailCard {
    FieldRow(
      label = Strings.transactionPayee,
      value = form.payeeName,
      onClick = { onEdit(Payee) },
    )
    CardDivider()
    FieldRow(
      label = Strings.transactionCategory,
      value =
        when {
          form.isOffBudget -> Strings.transactionsOffBudget
          form.isTransfer -> Strings.transactionsTransfer
          else -> form.categoryName
        },
      enabled = !form.isOffBudget && !form.isTransfer,
      onClick = { onEdit(Category) },
    )
    CardDivider()
    FieldRow(
      label = Strings.transactionAccount,
      value = form.accountName,
      onClick = { onEdit(Account) },
    )
    CardDivider()
    FieldRow(
      label = Strings.transactionDate,
      value = "${draft.date.dayOfWeek.stringLong()} ${draft.date.formatted()}",
      onClick = { onEdit(Date) },
    )
    CardDivider()
    NotesRow(notes = draft.notes, tags = form.options.tags, onClick = { onEdit(Notes) })
    CardDivider()
    StatusRow(draft = draft, onUnlock = onUnlock, onAction = onAction)
  }
}

@Composable
private fun NotesRow(notes: String?, tags: ImmutableList<TagLabel>, onClick: () -> Unit) {
  val text = notes?.takeIf { it.isNotBlank() }
  FieldRow(
    label = Strings.transactionNotes,
    value = text,
    showChevron = false,
    onClick = onClick,
    valueContent =
      text?.let { str ->
        { modifier ->
          TaggedNotes(
            modifier = modifier,
            notes = str,
            tags = tags,
            fontSize = ValueSize,
            color = colors.pageText,
            maxLines = 2,
          )
        }
      },
  )
}

@Composable
private fun StatusRow(
  draft: TransactionFields,
  onUnlock: () -> Unit,
  onAction: EditTransactionActionHandler,
) =
  if (draft.reconciled) {
    // Stays on until the unlock is confirmed
    SwitchRow(
      label = Strings.transactionReconciled,
      checked = true,
      onChange = { onUnlock() },
    )
  } else {
    SwitchRow(
      label = Strings.transactionCleared,
      checked = draft.cleared,
      onChange = { onAction(SetCleared(it)) },
    )
  }

@Composable
internal fun HeroAmount(
  amount: Amount,
  typed: String,
  isTyping: Boolean,
  isIncome: Boolean,
  isValid: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  fontSize: TextUnit = HeroAmountSize,
  cursorHeight: Dp = CursorHeight,
) {
  val lineColor = if (isTyping) colors.buttonPrimaryBackground else colors.tableBorder
  val textColor =
    when {
      isTyping && !isValid -> colors.errorText
      isIncome -> colors.numberPositive
      else -> colors.numberNegative
    }
  Row(
    modifier =
      modifier
        .clickable(onClickLabel = Strings.transactionAmount, onClick = onClick)
        .drawBehind { underline(lineColor, UnderlineWidth.toPx()) }
        .padding(UnderlinePadding),
    verticalAlignment = CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(CursorGap),
  ) {
    Text(
      text =
        if (isTyping) {
          AnnotatedString(typed.ifEmpty { EMPTY_AMOUNT })
        } else {
          amount.abs().formattedText()
        },
      fontSize = fontSize,
      fontWeight = Bold,
      letterSpacing = HeroAmountTracking,
      color = textColor,
      style = tabularFigures(),
      maxLines = 1,
    )
    if (isTyping) {
      Box(
        modifier =
          Modifier.size(CursorWidth, cursorHeight).background(colors.buttonPrimaryBackground),
      )
    }
  }
}

@Composable
internal fun FieldRow(
  label: String,
  value: String?,
  onClick: () -> Unit,
  enabled: Boolean = true,
  showChevron: Boolean = true,
  minHeight: Dp = FieldRowHeight,
  labelWidth: Dp = FieldLabelWidth,
  valueContent: (@Composable (Modifier) -> Unit)? = null,
) =
  Row(
    modifier =
      Modifier.fillMaxWidth()
        .clickable(enabled = enabled, onClickLabel = label, onClick = onClick)
        .heightIn(min = minHeight)
        .padding(FieldRowPadding),
    verticalAlignment = CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(FieldRowGap),
  ) {
    Text(
      modifier = Modifier.width(labelWidth),
      text = label,
      fontSize = FieldLabelSize,
      color = colors.pageTextSubdued,
    )
    if (valueContent != null) {
      valueContent(Modifier.weight(1f))
    } else {
      Text(
        modifier = Modifier.weight(1f),
        text = value ?: Strings.transactionNone,
        fontSize = ValueSize,
        color = if (value == null || !enabled) colors.pageTextSubdued else colors.pageText,
        fontStyle = if (value == null) Italic else null,
        maxLines = 2,
        overflow = Ellipsis,
      )
    }
    if (showChevron && enabled) {
      Icon(
        modifier = Modifier.size(ChevronSize).rotate(degrees = -90f),
        imageVector = MaterialIcons.ExpandMore,
        contentDescription = null,
        tint = colors.pageTextSubdued,
      )
    }
  }

@Composable
private fun SwitchRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) =
  Row(
    modifier = Modifier.fillMaxWidth().heightIn(min = FieldRowHeight).padding(SwitchRowPadding),
    verticalAlignment = CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(FieldRowGap),
  ) {
    Text(
      modifier = Modifier.weight(1f),
      text = label,
      fontSize = FieldLabelSize,
      color = colors.pageTextSubdued,
    )

    Switch(checked = checked, onCheckedChange = onChange, colors = colors.switch())
  }

private fun DrawScope.underline(color: Color, width: Float) =
  drawLine(
    color = color,
    start = Offset(x = 0f, y = size.height),
    end = Offset(x = size.width, y = size.height),
    strokeWidth = width,
  )

private fun Amount.abs(): Amount = if (this < Zero) -this else this

private fun Amount.signed(isIncome: Boolean): Amount = if (isIncome) abs() else -abs()

// A part's amount is typed and shown from its split's side, so an expense's parts read as positive
internal fun Amount.negatedIf(negate: Boolean): Amount = if (negate) -this else this

private val AmountPadding = PaddingValues(start = 8.dp, top = 8.dp, end = 8.dp, bottom = 4.dp)
private val AmountSpacing = 10.dp
private val ToggleWidth = 200.dp
private val ToggleTextSize = 13.sp
private val TogglePadding = PaddingValues(horizontal = 5.dp, vertical = 7.dp)
private val UnderlineWidth = 2.dp
private val UnderlinePadding = PaddingValues(start = 8.dp, end = 8.dp, bottom = 4.dp)
internal val CursorGap = 2.dp
internal val CursorWidth = 2.dp
private val CursorHeight = 40.dp
private val FieldRowHeight = 48.dp
private val FieldRowPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
private val SwitchRowPadding = PaddingValues(horizontal = 16.dp)
private val FieldRowGap = 12.dp
private val FieldLabelWidth = 84.dp
private val FieldLabelSize = 13.sp
private val ChevronSize = 16.dp
private val KeypadPadding = 8.dp
internal const val EMPTY_AMOUNT = "0"
