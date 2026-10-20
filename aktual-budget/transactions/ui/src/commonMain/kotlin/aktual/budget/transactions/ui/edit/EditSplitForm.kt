package aktual.budget.transactions.ui.edit

import aktual.budget.model.Amount
import aktual.budget.model.toSignedInputText
import aktual.budget.tags.domain.TagLabel
import aktual.budget.transactions.ui.tabularFigures
import aktual.budget.transactions.vm.edit.SplitPartForm
import aktual.budget.transactions.vm.edit.TransactionForm
import aktual.core.icons.material.Add
import aktual.core.icons.material.Clear
import aktual.core.icons.material.MaterialIcons
import aktual.core.l10n.Strings
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.BareIconButton
import aktual.core.ui.TonalButton
import aktual.core.ui.formatted
import aktual.core.ui.formattedString
import aktual.core.ui.formattedText
import aktual.core.ui.stringLong
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterStart
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEachIndexed
import kotlinx.collections.immutable.ImmutableList

// The split edit artboard of option A: a smaller total, since the parts' amounts matter more
@Composable
internal fun SplitTotal(
  amount: Amount,
  typed: String,
  isTyping: Boolean,
  isIncome: Boolean,
  isValid: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) =
  Column(
    modifier = modifier.fillMaxWidth().padding(TotalPadding),
    horizontalAlignment = CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(TotalSpacing),
  ) {
    HeroAmount(
      amount = amount,
      typed = typed,
      isTyping = isTyping,
      isIncome = isIncome,
      isValid = isValid,
      onClick = onClick,
      fontSize = TotalSize,
      cursorHeight = TotalCursorHeight,
    )
    Text(
      text =
        if (isIncome) Strings.transactionSplitTotalIncome else Strings.transactionSplitTotalExpense,
      fontSize = TotalLabelSize,
      color = colors.pageTextSubdued,
    )
  }

// The payee on a row of its own, then the account and date side by side
@Composable
internal fun SplitFieldsCard(form: TransactionForm, onEdit: (EditField) -> Unit) = DetailCard {
  val date = form.draft.date
  FieldRow(
    label = Strings.transactionPayee,
    value = form.payeeName,
    showChevron = false,
    minHeight = CompactRowHeight,
    labelWidth = CompactLabelWidth,
    onClick = { onEdit(Payee) },
  )
  CardDivider()
  Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
    CompactField(
      modifier = Modifier.weight(1f),
      label = Strings.transactionAccount,
      value = form.accountName,
      onClick = { onEdit(Account) },
    )
    VerticalDivider(color = colors.tableBorder)
    CompactField(
      modifier = Modifier.weight(1f),
      label = Strings.transactionDate,
      value = "${date.dayOfWeek.stringLong()} ${date.formatted()}",
      onClick = { onEdit(Date) },
    )
  }
}

@Composable
private fun CompactField(
  label: String,
  value: String?,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) =
  Box(
    modifier =
      modifier
        .fillMaxHeight()
        .clickable(onClickLabel = label, onClick = onClick)
        .heightIn(min = CompactRowHeight)
        .padding(CompactFieldPadding),
    contentAlignment = CenterStart,
  ) {
    Text(
      text = value ?: Strings.transactionNone,
      fontSize = CompactValueSize,
      color = if (value == null) colors.pageTextSubdued else colors.pageText,
      fontStyle = if (value == null) Italic else null,
      maxLines = 2,
      overflow = Ellipsis,
    )
  }

// The parts, each with an amount that the keypad edits in place, and what's left to assign
// while they don't add up
@Composable
internal fun SplitParts(
  form: TransactionForm,
  fieldState: EditFieldState,
  isIncome: Boolean,
  onAction: EditTransactionActionHandler,
  modifier: Modifier = Modifier,
) =
  Column(modifier = modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(SplitHeaderPadding),
      verticalAlignment = CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Text(
        text = Strings.transactionSplitInto(form.parts.size).uppercase(),
        fontSize = SplitHeaderSize,
        fontWeight = SemiBold,
        letterSpacing = SplitHeaderTracking,
        color = colors.pageTextLight,
      )
      AddSplitButton(onClick = { onAction(AddSplit) })
    }

    DetailCard {
      form.parts.fastForEachIndexed { index, part ->
        if (index > 0) CardDivider()
        PartRow(
          part = part,
          isOffBudget = form.isOffBudget,
          isIncome = isIncome,
          tags = form.options.tags,
          fieldState = fieldState,
          onAction = onAction,
        )
      }
    }

    val remaining = form.splitRemaining
    if (remaining != null) {
      RemainingBanner(
        modifier = Modifier.padding(top = BannerInset),
        remaining = remaining.negatedIf(!isIncome),
        onFill = {
          val target = fillTarget(form, fieldState.active)
          if (fieldState.isTypingAmount) fieldState.close()
          onAction(FillRemaining(target))
        },
      )
    }
  }

// The part whose amount is being typed, or else the first one without an amount, or the last
private fun fillTarget(form: TransactionForm, active: ActiveField?) =
  active?.part
    ?: form.parts.firstOrNull { it.part.amount == Zero }?.part?.id
    ?: form.parts.last().part.id

@Composable
private fun AddSplitButton(onClick: () -> Unit) =
  TonalButton(
    modifier = Modifier.height(AddSplitHeight),
    icon = MaterialIcons.Add,
    text = Strings.transactionSplitAdd,
    contentPadding = AddSplitPadding,
    onClick = onClick,
  )

@Composable
private fun PartRow(
  part: SplitPartForm,
  isOffBudget: Boolean,
  isIncome: Boolean,
  tags: ImmutableList<TagLabel>,
  fieldState: EditFieldState,
  onAction: EditTransactionActionHandler,
) {
  val id = part.part.id
  val amount = part.part.amount
  Row(
    modifier = Modifier.fillMaxWidth().heightIn(min = PartRowHeight).padding(PartRowPadding),
    verticalAlignment = CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(PartRowGap),
  ) {
    Column(modifier = Modifier.weight(1f)) {
      PartText(
        text =
          when {
            isOffBudget -> Strings.transactionsOffBudget
            part.isTransfer -> Strings.transactionsTransfer
            else -> part.categoryName
          },
        placeholder = Strings.transactionSplitChooseCategory,
        label = Strings.transactionCategory,
        fontSize = ValueSize,
        enabled = !isOffBudget && !part.isTransfer,
        onClick = { fieldState.edit(Category, id) },
      )
      Row(horizontalArrangement = Arrangement.spacedBy(PartDetailGap)) {
        PartText(
          text = part.payeeName,
          placeholder = Strings.transactionSplitAddPayee,
          label = Strings.transactionPayee,
          fontSize = PartDetailSize,
          onClick = { fieldState.edit(Payee, id) },
        )
        PartText(
          modifier = Modifier.weight(1f, fill = false),
          text = part.part.notes?.takeIf { it.isNotBlank() },
          placeholder = Strings.transactionSplitAddNote,
          label = Strings.transactionNotes,
          fontSize = PartDetailSize,
          tags = tags,
          onClick = { fieldState.edit(Notes, id) },
        )
      }
    }

    PartAmount(
      amount = amount,
      shown = amount.negatedIf(!isIncome),
      typed = fieldState.amountText.text.toString(),
      isTyping = fieldState.active == ActiveField(EditField.Amount, id),
      isValid = fieldState.amountText.isValidAmount(),
      onClick = { fieldState.typeAmount(amount.negatedIf(!isIncome).toSignedInputText(), id) },
    )

    BareIconButton(
      imageVector = MaterialIcons.Clear,
      contentDescription = Strings.transactionSplitRemove,
      size = RemoveIconSize,
      onClick = {
        if (fieldState.active?.part == id) fieldState.close()
        onAction(RemoveSplit(id))
      },
    )
  }
}

@Composable
private fun PartText(
  text: String?,
  placeholder: String,
  label: String,
  fontSize: TextUnit,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  tags: ImmutableList<TagLabel>? = null,
) {
  val textModifier =
    modifier
      .clip(ChipShape)
      .clickable(enabled = enabled, onClickLabel = label, onClick = onClick)
      .padding(PartTextPadding)
  if (text != null && tags != null) {
    TaggedNotes(
      modifier = textModifier,
      notes = text,
      tags = tags,
      fontSize = fontSize,
      color = if (enabled) colors.pageText else colors.pageTextSubdued,
    )
  } else {
    Text(
      modifier = textModifier,
      text = text ?: placeholder,
      fontSize = fontSize,
      color = if (text == null || !enabled) colors.pageTextSubdued else colors.pageText,
      maxLines = 1,
      overflow = Ellipsis,
    )
  }
}

// shown is the amount from the split's side, so a part of an expense reads as positive
@Composable
private fun PartAmount(
  amount: Amount,
  shown: Amount,
  typed: String,
  isTyping: Boolean,
  isValid: Boolean,
  onClick: () -> Unit,
) {
  val textColor =
    when {
      isTyping && !isValid -> colors.errorText
      amount == Zero -> colors.pageTextSubdued
      amount > Zero -> colors.numberPositive
      else -> colors.numberNegative
    }
  val highlight =
    if (isTyping) {
      Modifier.background(colors.formInputBackgroundSelected)
        .border(PartAmountBorder, colors.formInputBorderSelected, PartAmountShape)
    } else {
      Modifier
    }
  Row(
    modifier =
      Modifier.widthIn(min = PartAmountWidth)
        .height(PartAmountHeight)
        .clip(PartAmountShape)
        .then(highlight)
        .clickable(onClickLabel = Strings.transactionAmount, onClick = onClick)
        .padding(PartAmountPadding),
    verticalAlignment = CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(CursorGap, Alignment.End),
  ) {
    Text(
      text =
        if (isTyping) AnnotatedString(typed.ifEmpty { EMPTY_AMOUNT }) else shown.formattedText(),
      fontSize = ValueSize,
      color = textColor,
      style = tabularFigures(),
      maxLines = 1,
    )
    if (isTyping) {
      Box(
        modifier =
          Modifier.size(CursorWidth, PartCursorHeight).background(colors.buttonPrimaryBackground),
      )
    }
  }
}

// remaining is from the split's side too: positive while there's some of the total left
@Composable
private fun RemainingBanner(remaining: Amount, onFill: () -> Unit, modifier: Modifier = Modifier) =
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .background(colors.warningBackground, BannerShape)
        .padding(BannerPadding),
    verticalAlignment = CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(BannerGap),
  ) {
    Text(
      modifier = Modifier.weight(1f),
      text =
        if (remaining > Zero) {
          Strings.transactionSplitLeft(remaining.formattedString())
        } else {
          Strings.transactionSplitOver((-remaining).formattedString())
        },
      fontSize = BannerTextSize,
      color = colors.warningTextDark,
    )
    Text(
      modifier =
        Modifier.clip(FillShape)
          .border(FillBorder, colors.warningText, FillShape)
          .clickable(onClick = onFill)
          .padding(FillPadding),
      text = Strings.transactionSplitFill,
      fontSize = FillTextSize,
      fontWeight = SemiBold,
      color = colors.warningText,
    )
  }

private val TotalPadding = PaddingValues(start = 8.dp, top = 4.dp, end = 8.dp)
private val TotalSpacing = 4.dp
private val TotalSize = 36.sp
private val TotalCursorHeight = 32.dp
private val TotalLabelSize = 12.sp
private val CompactRowHeight = 44.dp
private val CompactLabelWidth = 64.dp
private val CompactFieldPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
private val CompactValueSize = 14.sp
private val ChipShape = RoundedCornerShape(6.dp)
private val AddSplitHeight = 32.dp
private val AddSplitPadding = PaddingValues(horizontal = 10.dp)
private val PartRowHeight = 56.dp
private val PartRowPadding = PaddingValues(start = 8.dp, top = 4.dp, bottom = 4.dp)
private val PartRowGap = 4.dp
private val PartTextPadding = PaddingValues(horizontal = 6.dp, vertical = 5.dp)
private val PartDetailGap = 2.dp
private val PartDetailSize = 12.sp
private val PartAmountWidth = 86.dp
private val PartAmountHeight = 36.dp
private val PartAmountShape = RoundedCornerShape(6.dp)
private val PartAmountBorder = 2.dp
private val PartAmountPadding = PaddingValues(horizontal = 8.dp)
private val PartCursorHeight = 18.dp
private val RemoveIconSize = 16.dp
private val BannerInset = 10.dp
private val BannerShape = RoundedCornerShape(8.dp)
private val BannerPadding = PaddingValues(start = 14.dp, top = 6.dp, end = 6.dp, bottom = 6.dp)
private val BannerGap = 8.dp
private val BannerTextSize = 13.sp
private val FillShape = RoundedCornerShape(15.dp)
private val FillBorder = 1.dp
private val FillPadding = PaddingValues(horizontal = 12.dp, vertical = 7.dp)
private val FillTextSize = 12.sp
