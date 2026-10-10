package aktual.budget.transactions.ui.edit

import aktual.budget.model.Amount
import aktual.budget.model.evaluateAmountInput
import aktual.budget.model.toInputText
import aktual.budget.transactions.ui.tabularFigures
import aktual.budget.transactions.vm.edit.TransactionEditMode
import aktual.core.icons.material.ExpandMore
import aktual.core.icons.material.MaterialIcons
import aktual.core.l10n.Strings
import aktual.core.ui.AktualSlidingToggleButton
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AmountKeypad
import aktual.core.ui.BottomSpacing
import aktual.core.ui.RootOverlayContent
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
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.persistentListOf

internal fun TextFieldState.isValidAmount(): Boolean = evaluateAmountInput(text.toString()) != null

// The edit artboard of option A: the amount up top, typed with the docked keypad, then a card of
// rows that each open a picker
@Composable
internal fun EditTransactionForm(
  edit: TransactionEditMode.Edit,
  amountText: TextFieldState,
  activeField: EditField?,
  onActiveField: (EditField?) -> Unit,
  onUnlock: () -> Unit,
  onAction: EditTransactionActionHandler,
  scrollState: ScrollState,
  contentPadding: PaddingValues,
  modifier: Modifier = Modifier,
) {
  val draft = edit.draft
  val isTyping = activeField == EditField.Amount

  // Typed amounts are unsigned, and pick up their sign from the expense/income toggle. Zero has no
  // sign of its own, so the toggle is remembered for it
  var zeroIsIncome by remember { mutableStateOf(false) }
  val isIncome = if (draft.amount == Zero) zeroIsIncome else draft.amount > Zero

  val latestIsIncome by rememberUpdatedState(isIncome)
  if (isTyping) {
    LaunchedEffect(amountText) {
      snapshotFlow { amountText.text.toString() }
        .collect { text ->
          evaluateAmountInput(text)?.let { onAction(SetAmount(it.signed(latestIsIncome))) }
        }
    }
  }

  val density = LocalDensity.current
  var keypadHeight by remember { mutableStateOf(0.dp) }
  val keypad: @Composable (Modifier) -> Unit = { keypadModifier ->
    DockedKeypad(
      modifier = keypadModifier,
      amountText = amountText,
      onDone = { onActiveField(null) },
    )
  }

  Column(modifier = modifier.fillMaxSize()) {
    Column(
      modifier =
        Modifier.weight(1f)
          .verticalScrollWithBar(scrollState)
          .padding(contentPadding)
          .padding(horizontal = CardInset),
      verticalArrangement = Arrangement.spacedBy(CardSpacing),
    ) {
      Column(
        modifier = Modifier.fillMaxWidth().padding(AmountPadding),
        horizontalAlignment = CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AmountSpacing),
      ) {
        AktualSlidingToggleButton(
          modifier = Modifier.width(ToggleWidth),
          selected = isIncome,
          options = persistentListOf(false, true),
          onSelect = { income ->
            zeroIsIncome = income
            onAction(SetAmount(draft.amount.signed(income)))
          },
          string = { income ->
            if (income) Strings.transactionIncome else Strings.transactionExpense
          },
          fontSize = ToggleTextSize,
          itemPadding = TogglePadding,
        )

        HeroAmount(
          amount = draft.amount,
          typed = amountText.text.toString(),
          isTyping = isTyping,
          isIncome = isIncome,
          isValid = amountText.isValidAmount(),
          onClick = {
            amountText.setTextAndPlaceCursorAtEnd(draft.amount.toInputText())
            onActiveField(EditField.Amount)
          },
        )
      }

      FieldsCard(
        edit = edit,
        onActiveField = onActiveField,
        onUnlock = onUnlock,
        onAction = onAction,
      )

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
  edit: TransactionEditMode.Edit,
  onActiveField: (EditField?) -> Unit,
  onUnlock: () -> Unit,
  onAction: EditTransactionActionHandler,
) {
  val draft = edit.draft
  DetailCard {
    FieldRow(
      label = Strings.transactionPayee,
      value = edit.payeeName,
      onClick = { onActiveField(Payee) },
    )
    CardDivider()
    FieldRow(
      label = Strings.transactionCategory,
      value = if (edit.isOffBudget) Strings.transactionsOffBudget else edit.categoryName,
      enabled = !edit.isOffBudget,
      onClick = { onActiveField(Category) },
    )
    CardDivider()
    FieldRow(
      label = Strings.transactionAccount,
      value = edit.accountName,
      onClick = { onActiveField(Account) },
    )
    CardDivider()
    FieldRow(
      label = Strings.transactionDate,
      value = "${draft.date.dayOfWeek.stringLong()} ${draft.date.formatted()}",
      onClick = { onActiveField(Date) },
    )
    CardDivider()
    FieldRow(
      label = Strings.transactionNotes,
      value = draft.notes?.takeIf { it.isNotBlank() },
      showChevron = false,
      onClick = { onActiveField(Notes) },
    )
    CardDivider()
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
  }
}

@Composable
private fun HeroAmount(
  amount: Amount,
  typed: String,
  isTyping: Boolean,
  isIncome: Boolean,
  isValid: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
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
      fontSize = HeroAmountSize,
      fontWeight = Bold,
      letterSpacing = HeroAmountTracking,
      color = textColor,
      style = tabularFigures(),
      maxLines = 1,
    )
    if (isTyping) {
      Box(
        modifier =
          Modifier.size(CursorWidth, CursorHeight).background(colors.buttonPrimaryBackground),
      )
    }
  }
}

@Composable
private fun FieldRow(
  label: String,
  value: String?,
  onClick: () -> Unit,
  enabled: Boolean = true,
  showChevron: Boolean = true,
) =
  Row(
    modifier =
      Modifier.fillMaxWidth()
        .clickable(enabled = enabled, onClickLabel = label, onClick = onClick)
        .heightIn(min = FieldRowHeight)
        .padding(FieldRowPadding),
    verticalAlignment = CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(FieldRowGap),
  ) {
    Text(
      modifier = Modifier.width(FieldLabelWidth),
      text = label,
      fontSize = FieldLabelSize,
      color = colors.pageTextSubdued,
    )
    Text(
      modifier = Modifier.weight(1f),
      text = value ?: Strings.transactionNone,
      fontSize = ValueSize,
      color = if (value == null || !enabled) colors.pageTextSubdued else colors.pageText,
      fontStyle = if (value == null) Italic else null,
      maxLines = 2,
      overflow = Ellipsis,
    )
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

private val AmountPadding = PaddingValues(start = 8.dp, top = 8.dp, end = 8.dp, bottom = 4.dp)
private val AmountSpacing = 10.dp
private val ToggleWidth = 200.dp
private val ToggleTextSize = 13.sp
private val TogglePadding = PaddingValues(horizontal = 5.dp, vertical = 7.dp)
private val UnderlineWidth = 2.dp
private val UnderlinePadding = PaddingValues(start = 8.dp, end = 8.dp, bottom = 4.dp)
private val CursorGap = 2.dp
private val CursorWidth = 2.dp
private val CursorHeight = 40.dp
private val FieldRowHeight = 48.dp
private val FieldRowPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
private val SwitchRowPadding = PaddingValues(horizontal = 16.dp)
private val FieldRowGap = 12.dp
private val FieldLabelWidth = 84.dp
private val FieldLabelSize = 13.sp
private val ChevronSize = 16.dp
private val KeypadPadding = 8.dp
private const val EMPTY_AMOUNT = "0"
